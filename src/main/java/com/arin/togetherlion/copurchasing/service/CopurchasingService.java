package com.arin.togetherlion.copurchasing.service;

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

        final User deleter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        final User writer = copurchasing.getWriter();
        copurchasing.validateDelete(writer, deleter);
        // 포인트 다시 돌려줘야 함
        copurchasing.refund();

        copurchasingRepository.delete(copurchasing);
    }

    @Transactional
    public Long participate(CopurchasingParticipateRequest request) {
        final Copurchasing copurchasing = copurchasingRepository.findById(request.getCopurchasingId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        final User participant = userRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이 로직을 copurchasing 안에 넣어도 될까?
        final int paymentCost = copurchasing.getPaymentCost(request.getPurchaseNumber());
        participant.pay(paymentCost);

        Participation participation = Participation.builder()
                .purchaseNumber(request.getPurchaseNumber())
                .user(participant)
                .payment(paymentCost)
                .build();

        copurchasing.addParticipation(participation);
        return participationRepository.save(participation).getId();
    }
}
