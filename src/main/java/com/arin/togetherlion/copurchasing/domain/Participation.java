package com.arin.togetherlion.copurchasing.domain;

import com.arin.togetherlion.common.BaseTimeEntity;
import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.common.ErrorCode;
import com.arin.togetherlion.point.domain.Point;
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
public class Participation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_number", nullable = false)
    private int purchaseNumber;

    @Column(name = "confirm_date")
    private LocalDateTime confirmDate;

    @Column(name = "payment_point")
    private Point paymentPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User participant;

    public boolean isConfirm() {
        if (confirmDate == null)
            return false;
        return true;
    }

    @Builder
    public Participation(int purchaseNumber, User participant, int payment) {
        validatePurchaseNumber(purchaseNumber);
        this.purchaseNumber = purchaseNumber;
        this.participant = participant;
        this.paymentPoint = new Point(payment);
    }

    private void validatePurchaseNumber(int purchaseNumber) {
        if (purchaseNumber < 1)
            throw new IllegalArgumentException("상품 구매 개수는 1 이상이여야 합니다.");
    }

    public boolean isParticipant(User participant) {
        if (this.participant.isSameUser(participant))
            return true;
        return false;
    }

    public void validateDeleteParticipation(User writer, User deleter) {
        if (!deleter.isSameUser(participant))
            throw new CustomException(ErrorCode.NO_PERMISSION);
        if (deleter.isSameUser(writer))
            throw new IllegalArgumentException("작성자는 참여 취소가 불가합니다.");
    }

    public void refund() {
        participant.refund(paymentPoint.getAmount());
    }
}
