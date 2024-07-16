package com.arin.togetherlion.copurchasing.domain;

import com.arin.togetherlion.common.BaseTimeEntity;
import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.common.ErrorCode;
import com.arin.togetherlion.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Embedded
    private Participations participations = new Participations();

    public void addParticipation(Participation participation) {
        validateParticipation(participation.getParticipant());
        participations.add(participation);
    }

    public void deleteParticipation(Participation participation) {
        getParticipations().getParticipations().remove(participation);
    }

    @Builder
    public Copurchasing(String title, String content, ProductTotalCost productTotalCost, ShippingCost shippingCost, String productUrl, LocalDateTime expirationDate, int productMinNumber, int productMaxNumber, LocalDateTime deadlineDate, String purchasePhotoUrl, LocalDateTime tradeDate, User writer, int purchaseNumber) {
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
        if (isDeadlineExpired()) {
            if (participations.getTotalProductNumber() >= productMinNumber)
                return true;
        }
        return false;
    }

    private boolean isDeadlineExpired() {
        if (getDeadlineDate().isBefore(LocalDateTime.now()))
            return true;
        return false;
    }

    private void validateParticipation(User participant) {
        if (participations.isParticipant(participant))
            throw new CustomException(ErrorCode.CANT_JOIN);
        if (isDeadlineExpired())
            throw new IllegalArgumentException("모집 기한이 만료된 공동구매는 참여할 수 없습니다.");
        if (participations.getTotalProductNumber() >= productMaxNumber)
            throw new IllegalArgumentException("최대 상품 개수가 모집된 공동구매는 참여할 수 없습니다.");
    }

    private int calculateIndividualCost(int totalCost, int totalProductNumber) {
        return (int) Math.ceil((double) totalCost / totalProductNumber);
    }

    public int getPaymentCost(int purchaseNumber) {
        final int totalCost = getShippingCost().getValue() + getProductTotalCost().getValue();
        if (isStarted()) {
            final int individualCost = calculateIndividualCost(totalCost, participations.getTotalProductNumber());
            return individualCost * purchaseNumber;
        }
        return calculateIndividualCost(totalCost, productMinNumber) * purchaseNumber;
    }

    public void validateDelete(User deleter) {
        if (!getWriter().isSameUser(deleter))
            throw new CustomException(ErrorCode.NO_PERMISSION);
        if (isStarted())
            throw new IllegalArgumentException("이미 시작된 공동구매 게시물은 삭제할 수 없습니다.");
    }

    public void refund() {
        participations.refund();
    }
}
