package com.arin.togetherlion.copurchasing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingCostTest {

    @Test
    @DisplayName("ShippingCost 에 음수 값을 넣으면 예외가 발생한다.")
    void negativeValueThrowException() {
        // given
        int wrongValue = -1000;

        // when
        // then
        assertThatThrownBy(() -> new ShippingCost(wrongValue))
                .isInstanceOf(IllegalArgumentException.class);
    }
}