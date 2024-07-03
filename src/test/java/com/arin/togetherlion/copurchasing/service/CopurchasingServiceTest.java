package com.arin.togetherlion.copurchasing.service;

import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.copurchasing.domain.Copurchasing;
import com.arin.togetherlion.copurchasing.domain.Participation;
import com.arin.togetherlion.copurchasing.domain.ProductTotalCost;
import com.arin.togetherlion.copurchasing.domain.ShippingCost;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.repository.CopurchasingRepository;
import com.arin.togetherlion.copurchasing.repository.ParticipationRepository;
import com.arin.togetherlion.user.domain.User;
import com.arin.togetherlion.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@DataJpaTest
class CopurchasingServiceTest {

    @Autowired
    private CopurchasingRepository copurchasingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParticipationRepository participationRepository;
    private CopurchasingService copurchasingService;

    private User writer;
    private Copurchasing testCopurchasing;

    @BeforeEach
    void setUp() {
        copurchasingService = new CopurchasingService(copurchasingRepository, userRepository);

        writer = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        testCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusDays(5))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .build();
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 게시물을 작성할 시 예외가 발생한다.")
    void notExistedUserCreate() throws Exception {
        //given
        Long notExistedUserId = 0L;
        final CopurchasingCreateRequest request = CopurchasingCreateRequest.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(10000)
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusDays(5))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(3000)
                .writerId(notExistedUserId)
                .build();

        //when
        //then
        Assertions.assertThatThrownBy(() -> copurchasingService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자는 공동구매 게시물을 작성할 수 있다.")
    void create() throws Exception {
        //given
        Long writerId = userRepository.save(writer).getId();
        Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        //when
        //then
        Assertions.assertThat(copurchasingRepository.existsById(copurchasingId)).isTrue();
    }

    @Test
    @DisplayName("작성자는 공동구매 게시물을 삭제할 수 있다.")
    void delete() {
        // given
        Long userId = userRepository.save(writer).getId();
        Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        // when
        copurchasingService.delete(userId, copurchasingId);

        // then
        Assertions.assertThat(copurchasingRepository.existsById(testCopurchasing.getId())).isFalse();
    }

    @Test
    @DisplayName("작성자는 공동구매 게시물을 삭제할 수 있다. (모집 기간이 만료됐지만 최소 상품 개수가 모집되지 않은 경우")
    void delete2() {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing notStartedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(3)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().minusDays(3))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .build();
        Long notStartedCopurchasingId = copurchasingRepository.save(notStartedCopurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        userRepository.save(user);

        final Participation participation = new Participation(1, user);
        notStartedCopurchasing.addParticipation(participation);
        participationRepository.save(participation);

        // when
        copurchasingService.delete(writerId, notStartedCopurchasingId);

        // then
        Assertions.assertThat(copurchasingRepository.existsById(notStartedCopurchasingId)).isFalse();
    }

    @Test
    @DisplayName("작성자가 아닌 사용자는 게시물을 삭제할 시 예외가 발생한다.")
    void notWriterDelete() {
        final User notWriter = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        // given
        userRepository.save(writer);
        Long notWriterId = userRepository.save(notWriter).getId();
        Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.delete(notWriterId, testCopurchasing.getId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("이미 시작된 공동구매 게시물은 삭제할 시 예외가 발생한다. (최대 상품 개수 이상 모집된 경우)")
    void startedDeleteWithMaxNumber() {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing startedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusDays(5))
                .productMaxNumber(1)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .build();
        Long startedCopurchasingId = copurchasingRepository.save(startedCopurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        userRepository.save(user);

        final Participation participation = new Participation(1, user);
        startedCopurchasing.addParticipation(participation);
        participationRepository.save(participation);

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.delete(writerId, startedCopurchasingId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 시작된 공동구매 게시물은 삭제할 시 예외가 발생한다. (모집 기간 만료 + 최소 상품 개수 이상 모집된 경우)")
    void startedDeleteWithMinNumber() {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing startedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().minusDays(3))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .build();
        Long startedCopurchasingId = copurchasingRepository.save(startedCopurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        userRepository.save(user);

        final Participation participation = new Participation(1, user);
        startedCopurchasing.addParticipation(participation);
        participationRepository.save(participation);

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.delete(writerId, startedCopurchasingId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}