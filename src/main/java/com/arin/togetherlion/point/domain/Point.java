package com.arin.togetherlion.point.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    private int amount;

    public Point(int amount) {
        validateAmount(amount);
        this.amount = amount;
    }

    public void plus(int amount) {
        validateAmount(amount);
        this.amount += amount;
    }

    public void minus(int amount) {
        validateAmount(this.amount - amount);
        this.amount -= amount;
    }

    private void validateAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("포인트는 음수가 될 수 없습니다.");
    }
}
