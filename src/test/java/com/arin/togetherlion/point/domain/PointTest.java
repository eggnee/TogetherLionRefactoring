package com.arin.togetherlion.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {

    @Test
    @DisplayName("Point 값에 음수 값을 넣으면 예외가 발생한다.")
    void pointFail() throws Exception {
        //given
        int wrongValue = -1000;

        //when
        //then
        assertThatThrownBy(() -> new Point(wrongValue))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 사용 시 포인트가 부족하다면 예외가 발생한다.")
    void useFail() throws Exception {
        //given
        Point wrongPoint = new Point(1000);

        //when
        //then
        assertThatThrownBy(() -> wrongPoint.use(2000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 충전 및 환급 시 음수 값이 들어온다면 예외가 발생한다.")
    void chargeOrRefundFail() throws Exception {
        //given
        Point wrongPoint = new Point(1000);

        //when
        //then
        assertThatThrownBy(() -> wrongPoint.chargeOrRefund(-1000))
                .isInstanceOf(IllegalArgumentException.class);
    }

}