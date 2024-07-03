package com.arin.togetherlion.copurchasing.domain;

import com.arin.togetherlion.common.BaseTimeEntity;
import com.arin.togetherlion.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Copurchasing extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(name = "product_total_cost", nullable = false)
    private ProductTotalCost productTotalCost;

    @Column(name = "shipping_cost", nullable = false)
    private ShippingCost shippingCost;

    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "product_min_number", nullable = false)
    private int productMinNumber;

    @Column(name = "product_max_number", nullable = false)
    private int productMaxNumber;

    @Column(name = "deadline_date", nullable = false)
    private LocalDateTime deadlineDate;

    @Column(name = "trade_date", nullable = false)
    private LocalDateTime tradeDate;

    @Column(name = "purchase_photo_url")
    private String purchasePhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();

    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    @Builder
    public Copurchasing(String title, String content, ProductTotalCost productTotalCost, ShippingCost shippingCost, String productUrl, LocalDateTime expirationDate, int productMinNumber, int productMaxNumber, LocalDateTime deadlineDate, String purchasePhotoUrl, LocalDateTime tradeDate, User writer) {
        validateNumber(productMinNumber, productMaxNumber);
        validateDate(deadlineDate, tradeDate);
        this.title = title;
        this.content = content;
        this.productTotalCost = productTotalCost;
        this.shippingCost = shippingCost;
        this.productUrl = productUrl;
        this.expirationDate = expirationDate;
        this.productMinNumber = productMinNumber;
        this.productMaxNumber = productMaxNumber;
        this.deadlineDate = deadlineDate;
        this.purchasePhotoUrl = purchasePhotoUrl;
        this.tradeDate = tradeDate;
        this.writer = writer;
    }

    private void validateNumber(int productMinNumber, int productMaxNumber) {
        if (productMaxNumber < productMinNumber)
            throw new IllegalArgumentException("최소 상품 개수는 최대 상품 개수보다 클 수 없습니다.");
    }

    private void validateDate(LocalDateTime deadlineDate, LocalDateTime tradeDate) {
        if (tradeDate.isBefore(deadlineDate))
            throw new IllegalArgumentException("거래 희망 일자는 모집 완료 일자 이후여야 합니다.");
    }

    public boolean isStarted() {
        if (!participations.isEmpty()) {
            if (this.getProductMaxNumber() <= participations.size())
                return true;
            if (this.getDeadlineDate().isBefore(LocalDateTime.now()) && this.getProductMinNumber() <= participations.size())
                return true;
        }
        return false;
    }
}
