package com.arin.togetherlion.copurchasing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTotalCost {

    @Column(name = "product_total_cost", nullable = false)
    private int value;

    public ProductTotalCost(final int value) {
        validate(value);
        this.value = value;
    }

    private void validate(int value) {
        if (value < 0)
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
    }
}
