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

    private void validateAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("포인트는 음수가 될 수 없습니다.");
    }

    public void add(int amount) {
        validateCalculationAmount(amount);
        this.amount += amount;
    }

    public void use(int amount) {
        validateCalculationAmount(amount);
        validateAmount(this.amount - amount);
        this.amount -= amount;
    }

    private void validateCalculationAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("포인트 연산에는 음수 사용이 불가합니다.");
    }
}
