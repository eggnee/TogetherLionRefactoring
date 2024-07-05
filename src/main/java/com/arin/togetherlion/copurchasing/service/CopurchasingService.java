package com.arin.togetherlion.copurchasing.service;

import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.common.ErrorCode;
import com.arin.togetherlion.copurchasing.domain.Copurchasing;
import com.arin.togetherlion.copurchasing.domain.Participation;
import com.arin.togetherlion.copurchasing.domain.ProductTotalCost;
import com.arin.togetherlion.copurchasing.domain.ShippingCost;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingParticipateRequest;
import com.arin.togetherlion.copurchasing.repository.CopurchasingRepository;
import com.arin.togetherlion.copurchasing.repository.ParticipationRepository;
import com.arin.togetherlion.user.UserService;
import com.arin.togetherlion.user.domain.User;
import com.arin.togetherlion.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CopurchasingService {

    private final CopurchasingRepository copurchasingRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final UserService userService;

    @Transactional
    public Long create(CopurchasingCreateRequest request) {
        final User writer = userRepository.findById(request.getWriterId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        final Copurchasing copurchasing = Copurchasing.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .productTotalCost(new ProductTotalCost(request.getProductTotalCost()))
                .shippingCost(new ShippingCost(request.getShippingCost()))
                .productUrl(request.getProductUrl())
                .expirationDate(request.getExpirationDate())
                .productMinNumber(request.getProductMinNumber())
                .productMaxNumber(request.getProductMaxNumber())
                .deadlineDate(request.getDeadlineDate())
                .tradeDate(request.getTradeDate())
                .purchasePhotoUrl(request.getPurchasePhotoUrl())
                .writer(writer)
                .build();

        return copurchasingRepository.save(copurchasing).getId();
    }

    @Transactional
    public void delete(Long userId, Long copurchasingId) {
        final Copurchasing copurchasing = copurchasingRepository.findById(copurchasingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        User writer = copurchasing.getWriter();
        if (!writer.isSameUser(userId))
            throw new CustomException(ErrorCode.NO_PERMISSION);

        if (copurchasing.isStarted())
            throw new IllegalArgumentException("이미 시작된 공동구매 게시물은 삭제할 수 없습니다.");

        copurchasingRepository.delete(copurchasing);
    }

    @Transactional
    public Long participate(CopurchasingParticipateRequest request) {
        final Copurchasing copurchasing = copurchasingRepository.findById(request.getCopurchasingId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        if (copurchasing.isStarted())
            throw new IllegalArgumentException("이미 시작된 공동구매 게시물은 삭제할 수 없습니다.");

        if (copurchasing.isDeadlineExpired())
            throw new IllegalArgumentException("모집 기한이 만료된 공동구매 게시물은 삭제할 수 없습니다.");

        final User participant = userRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (copurchasing.getWriter().isSameUser(participant.getId()))
            throw new CustomException(ErrorCode.CANT_JOIN);

        final int paymentCost = calculatePaymentCost(copurchasing, request.getPurchaseNumber());
        userService.usePoint(participant, paymentCost);

        Participation participation = Participation.builder()
                .purchaseNumber(request.getPurchaseNumber())
                .user(participant)
                .payment(paymentCost)
                .build();

        copurchasing.addParticipation(participation);
        return participationRepository.save(participation).getId();
    }

    private int calculatePaymentCost(Copurchasing copurchasing, int purchaseNumber) {
        final int totalCost = copurchasing.getShippingCost().getValue() + copurchasing.getProductTotalCost().getValue();
        final int individualCost = (int) Math.ceil((double) totalCost / copurchasing.getProductMinNumber());
        return individualCost * purchaseNumber;
    }

    @Transactional
    public void getCopurchasingState(Long copurchasingId) {
        final Copurchasing copurchasing = copurchasingRepository.findById(copurchasingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        if (copurchasing.isStarted()) {

        }
    }
}
