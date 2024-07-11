package com.arin.togetherlion.copurchasing.service;

import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.common.ErrorCode;
import com.arin.togetherlion.copurchasing.domain.Copurchasing;
import com.arin.togetherlion.copurchasing.domain.Participation;
import com.arin.togetherlion.copurchasing.domain.ProductTotalCost;
import com.arin.togetherlion.copurchasing.domain.ShippingCost;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationDeleteRequest;
import com.arin.togetherlion.copurchasing.repository.CopurchasingRepository;
import com.arin.togetherlion.copurchasing.repository.ParticipationRepository;
import com.arin.togetherlion.user.UserService;
import com.arin.togetherlion.user.domain.User;
import com.arin.togetherlion.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

@DataJpaTest
class CopurchasingServiceTest {

    @Autowired
    private CopurchasingRepository copurchasingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParticipationRepository participationRepository;

    private UserService userService;
    private CopurchasingService copurchasingService;

    private User writer;
    private Copurchasing testCopurchasing;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        copurchasingService = new CopurchasingService(copurchasingRepository, userRepository, participationRepository, userService);

        writer = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        writer.getPoint().add(100000);

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
                .purchaseNumber(1)
                .build();
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 게시물을 작성할 시 예외가 발생한다.")
    void notExistedUserCreate() throws Exception {
        //given
        final Long notExistedUserId = 0L;
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
                .purchaseNumber(1)
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
        final Long writerId = userRepository.save(writer).getId();
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
                .writerId(writerId)
                .purchaseNumber(1)
                .build();
        final Long copurchasingId = copurchasingService.create(request);

        //when
        //then
        Assertions.assertThat(copurchasingRepository.existsById(copurchasingId)).isTrue();
    }

    @Test
    @DisplayName("작성자는 공동구매 게시물을 삭제할 수 있다.")
    void delete() {
        // given
        final Long userId = userRepository.save(writer).getId();
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        // when
        copurchasingService.delete(userId, copurchasingId);

        // then
        Assertions.assertThat(copurchasingRepository.existsById(testCopurchasing.getId())).isFalse();
        Assertions.assertThat(writer.getPoint().getAmount()).isEqualTo(100000);
    }

    @Test
    @DisplayName("작성자는 공동구매 게시물을 삭제할 수 있다. (모집 기간이 만료됐지만 최소 상품 개수가 모집되지 않은 경우)")
    void deleteWithDeadline() {
        // given
        final Long writerId = userRepository.save(writer).getId();
        final Copurchasing notStartedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(3)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusDays(3))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .purchaseNumber(1)
                .build();
        final Long notStartedCopurchasingId = copurchasingRepository.save(notStartedCopurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        userRepository.save(user);
        user.getPoint().add(5000);

        final Participation participation = new Participation(1, user, notStartedCopurchasing.getPaymentCost(1));
        user.pay(notStartedCopurchasing.getPaymentCost(participation.getPurchaseNumber()));
        notStartedCopurchasing.addParticipation(participation);
        participationRepository.save(participation);

        Assertions.assertThat(user.getPoint().getAmount()).isEqualTo(3666);

        // when
        copurchasingService.delete(writerId, notStartedCopurchasingId);

        // then
        Assertions.assertThat(copurchasingRepository.existsById(notStartedCopurchasingId)).isFalse();
        Assertions.assertThat(user.getPoint().getAmount()).isEqualTo(5000);
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
        final Long notWriterId = userRepository.save(notWriter).getId();
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.delete(notWriterId, testCopurchasing.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NO_PERMISSION);
    }

    @Test
    @DisplayName("이미 시작된 공동구매 게시물은 삭제할 시 예외가 발생한다.")
    void startedDeleteWithMinNumber() throws InterruptedException {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing startedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusSeconds(1))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .purchaseNumber(1)
                .build();
        final Long startedCopurchasingId = copurchasingRepository.save(startedCopurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        userRepository.save(user);

        final Participation participation = new Participation(1, user, 0);
        startedCopurchasing.addParticipation(participation);
        participationRepository.save(participation);

        Thread.sleep(1000);

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.delete(writerId, startedCopurchasingId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자는 공동구매에 참여할 수 있다.")
    void participationCreate() {
        // given
        final Long userId = userRepository.save(writer).getId();
        final Copurchasing copurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(3)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusDays(3))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .purchaseNumber(1)
                .build();
        final Long copurchasingId = copurchasingRepository.save(copurchasing).getId();

        final User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        final Long participantId = userRepository.save(user).getId();
        user.getPoint().add(10000);

        final ParticipationCreateRequest request = ParticipationCreateRequest.builder()
                .participantId(participantId)
                .purchaseNumber(3)
                .copurchasingId(copurchasingId)
                .build();

        // when
        final Long participateId = copurchasingService.participationCreate(request);

        // then
        Assertions.assertThat(participationRepository.existsById(participateId)).isTrue();
        final int userPointAmount = user.getPoint().getAmount();
        final int paymentPointAmount = participationRepository.findById(participateId).get().getPaymentPoint().getAmount();
        Assertions.assertThat(userPointAmount).isEqualTo(5998);
        Assertions.assertThat(paymentPointAmount).isEqualTo(4002);
    }

    @Test
    @DisplayName("중복된 사용자가 공동구매에 참여할 시 예외가 발생한다.")
    void writerParticipationCreate() {
        // given
        final Long writerId = userRepository.save(writer).getId();
        writer.getPoint().add(10000);
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();
        final Participation participation = participationRepository.save(new Participation(1, writer, 1334));
        testCopurchasing.addParticipation(participation);

        final ParticipationCreateRequest request = ParticipationCreateRequest.builder()
                .participantId(writerId)
                .purchaseNumber(1)
                .copurchasingId(copurchasingId)
                .build();

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationCreate(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CANT_JOIN);
    }

    @Test
    @DisplayName("모집 기한이 만료된 공동구매에 참여할 시 예외가 발생한다.")
    void startedParticipationCreateWithDeadline() throws InterruptedException {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing startedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusSeconds(1))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .purchaseNumber(1)
                .build();

        Thread.sleep(1000);

        final Long startedCopurchasingId = copurchasingRepository.save(startedCopurchasing).getId();

        final User participant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        final Long participantId = userRepository.save(participant).getId();
        participant.getPoint().add(10000);

        final ParticipationCreateRequest request = ParticipationCreateRequest.builder()
                .participantId(participantId)
                .purchaseNumber(1)
                .copurchasingId(startedCopurchasingId)
                .build();

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationCreate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .extracting("message")
                .isEqualTo("모집 기한이 만료된 공동구매는 참여할 수 없습니다.");
    }

    @Test
    @DisplayName("최대 상품 개수가 모집된 공동구매에 참여할 시 예외가 발생한다.")
    void startedParticipationCreateWithMaxNumber() throws InterruptedException {
        // given
        Long writerId = userRepository.save(writer).getId();
        final Copurchasing startedCopurchasing = Copurchasing.builder()
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
                .purchaseNumber(5)
                .build();

        final Long startedCopurchasingId = copurchasingRepository.save(startedCopurchasing).getId();
        final Participation participation = participationRepository.save(new Participation(5, writer, 4000));
        startedCopurchasing.addParticipation(participation);

        final User participant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();
        final Long participantId = userRepository.save(participant).getId();
        participant.getPoint().add(10000);

        final ParticipationCreateRequest request = ParticipationCreateRequest.builder()
                .participantId(participantId)
                .purchaseNumber(1)
                .copurchasingId(startedCopurchasingId)
                .build();

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationCreate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .extracting("message")
                .isEqualTo("최대 상품 개수가 모집된 공동구매는 참여할 수 없습니다.");
    }

    @Test
    @DisplayName("참여자는 공동구매 참여를 취소할 수 있다.")
    void deleteParticipation() {
        // given
        final User participant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        userRepository.save(writer);
        final Long participantId = userRepository.save(participant).getId();
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();

        participant.getPoint().add(10000);
        final int paymentCost = testCopurchasing.getPaymentCost(1);
        final Participation participation = new Participation(1, participant, paymentCost);
        testCopurchasing.addParticipation(participation);
        final Long participationId = participationRepository.save(participation).getId();
        participant.pay(paymentCost);
        final ParticipationDeleteRequest participationDeleteRequest = new ParticipationDeleteRequest(participationId, participantId);

        Assertions.assertThat(participant.getPoint().getAmount()).isEqualTo(6000);

        // when
        copurchasingService.participationDelete(participationDeleteRequest);

        // then
        Assertions.assertThat(participationRepository.existsById(participationId)).isFalse();
        Assertions.assertThat(participant.getPoint().getAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("참여자는 공동구매 시작 이후 참여를 취소할 시 예외가 발생한다.")
    void deleteParticipationFailWithStart() throws InterruptedException {
        // given
        final User participant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        final Copurchasing startedCopurchasing = Copurchasing.builder()
                .title("title")
                .productMinNumber(1)
                .productTotalCost(new ProductTotalCost(1000))
                .purchasePhotoUrl("url")
                .tradeDate(LocalDateTime.now().plusDays(10))
                .deadlineDate(LocalDateTime.now().plusSeconds(1))
                .productMaxNumber(5)
                .content("content")
                .productUrl("url")
                .shippingCost(new ShippingCost(3000))
                .writer(writer)
                .purchaseNumber(1)
                .build();

        userRepository.save(writer);
        final Long participantId = userRepository.save(participant).getId();
        final Long copurchasingId = copurchasingRepository.save(startedCopurchasing).getId();

        participant.getPoint().add(10000);
        final int paymentCost = startedCopurchasing.getPaymentCost(1);
        final Participation participation = new Participation(1, participant, paymentCost);
        startedCopurchasing.addParticipation(participation);
        final Long participationId = participationRepository.save(participation).getId();
        participant.pay(paymentCost);
        final ParticipationDeleteRequest participationDeleteRequest = new ParticipationDeleteRequest(participationId, participantId);

        Thread.sleep(1000);

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationDelete(participationDeleteRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .extracting("message")
                .isEqualTo("이미 시작한 공동구매는 참여 취소가 불가합니다.");
    }

    @Test
    @DisplayName("참여자가 아닌 사용자가 참여를 취소할 시 예외가 발생한다.")
    void deleteParticipationFailWithParticipant() throws InterruptedException {
        // given
        final User participant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        final User notParticipant = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .build();

        userRepository.save(writer);
        final Long participantId = userRepository.save(participant).getId();
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();
        final Long notParticipantId = userRepository.save(notParticipant).getId();

        participant.getPoint().add(10000);
        final int paymentCost = testCopurchasing.getPaymentCost(1);
        final Participation participation = new Participation(1, participant, paymentCost);
        testCopurchasing.addParticipation(participation);
        final Long participationId = participationRepository.save(participation).getId();
        participant.pay(paymentCost);
        final ParticipationDeleteRequest participationDeleteRequest = new ParticipationDeleteRequest(participationId, notParticipantId);


        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationDelete(participationDeleteRequest))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NO_PERMISSION);
    }

    @Test
    @DisplayName("작성자는 참여를 취소할 시 예외가 발생한다.")
    void deleteParticipationFailWithWriter() throws InterruptedException {
        // given
        final Long writerId = userRepository.save(writer).getId();
        final Long copurchasingId = copurchasingRepository.save(testCopurchasing).getId();
        final Participation participation = participationRepository.save(new Participation(1, writer, 1334));
        testCopurchasing.addParticipation(participation);
        final ParticipationDeleteRequest participationDeleteRequest = new ParticipationDeleteRequest(participation.getId(), writerId);

        // when
        // then
        Assertions.assertThatThrownBy(() -> copurchasingService.participationDelete(participationDeleteRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .extracting("message")
                .isEqualTo("작성자는 참여 취소가 불가합니다.");
    }
}