package com.arin.togetherlion.copurchasing.domain;

import com.arin.togetherlion.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Period;

class CopurchasingTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .email("email")
                .password("1234")
                .nickname("nick")
                .build();
    }

    @Test
    @DisplayName("최소 상품 개수가 최대 상품 개수보다 크다면 예외가 발생한다.")
    void validateProductNumber() throws Exception {
        //given
        int productMinNumber = 10;
        int productMaxNumber = 5;

        //when
        //then
        Assertions.assertThatThrownBy(() -> Copurchasing.builder().title("title")
                        .productTotalCost(new ProductTotalCost(1000))
                        .shippingCost(new ShippingCost(1000))
                        .productUrl("url")
                        .productMinNumber(productMinNumber)
                        .productMaxNumber(productMaxNumber)
                        .deadlineDate(LocalDateTime.now())
                        .tradeDate(LocalDateTime.now().plus(Period.ofDays(1)))
                        .writer(user)
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("거래 희망 일자가 모집 완료 일자보다 이를 시 예외가 발생한다.")
    void validateDate() throws Exception {
        //given
        LocalDateTime wrongDeadlineDate = LocalDateTime.now().plus(Period.ofDays(10));
        LocalDateTime wrongTradeDate = wrongDeadlineDate.minus(Period.ofDays(5));

        //when
        //then
        Assertions.assertThatThrownBy(() -> Copurchasing.builder().title("title")
                        .productTotalCost(new ProductTotalCost(1000))
                        .shippingCost(new ShippingCost(1000))
                        .productUrl("url")
                        .productMinNumber(5)
                        .productMaxNumber(10)
                        .deadlineDate(wrongDeadlineDate)
                        .tradeDate(wrongTradeDate)
                        .writer(user)
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}