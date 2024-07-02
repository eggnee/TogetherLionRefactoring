package com.arin.togetherlion.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {

    @Test
    @DisplayName("Point 값에 음수 값을 넣으면 예외가 발생한다.")
    void point() throws Exception {
        //given
        int wrongValue = -1000;

        //when
        //then
        assertThatThrownBy(() -> new Point(wrongValue))
                .isInstanceOf(IllegalArgumentException.class);
    }

}