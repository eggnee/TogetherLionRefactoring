package com.arin.togetherlion.copurchasing.service;

import com.arin.togetherlion.copurchasing.domain.Copurchasing;
import com.arin.togetherlion.copurchasing.domain.Participation;
import com.arin.togetherlion.copurchasing.domain.ProductTotalCost;
import com.arin.togetherlion.copurchasing.domain.ShippingCost;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationDeleteRequest;
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
                .purchaseNumber(request.getPurchaseNumber())
                .build();

        participate(copurchasing, request.getPurchaseNumber(), writer);
        return copurchasingRepository.save(copurchasing).getId();
    }

    private Participation participate(Copurchasing copurchasing, int purchaseNumber, User participant) {
        final int paymentCost = copurchasing.getPaymentCost(purchaseNumber);
        participant.pay(paymentCost);
        final Participation participation = Participation.builder()
                .purchaseNumber(purchaseNumber)
                .participant(participant)
                .payment(paymentCost)
                .build();
        copurchasing.addParticipation(participation);
        return participation;
    }

    @Transactional
    public void delete(Long userId, Long copurchasingId) {
        final Copurchasing copurchasing = copurchasingRepository.findById(copurchasingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        final User deleter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        final User writer = copurchasing.getWriter();
        copurchasing.validateDelete(deleter);
        copurchasing.refund();

        copurchasingRepository.delete(copurchasing);
    }

    @Transactional
    public Long createParticipation(ParticipationCreateRequest request) {
        final Copurchasing copurchasing = copurchasingRepository.findById(request.getCopurchasingId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시물입니다."));

        final User participant = userRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        final Participation participation = participate(copurchasing, request.getPurchaseNumber(), participant);

        return participation.getId();
    }

    @Transactional
    public void deleteParticipation(ParticipationDeleteRequest request) {
        final Participation participation = participationRepository.findById(request.getParticipationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 참여입니다."));

        final Copurchasing copurchasing = copurchasingRepository.findByParticipation(participation)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매에 대한 참여입니다."));

        final User deleter = userRepository.findById(request.getDeleterId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (copurchasing.isStarted())
            throw new IllegalArgumentException("이미 시작한 공동구매는 참여 취소가 불가합니다.");

        participation.validateDeleteParticipation(copurchasing.getWriter(), deleter);

        participation.refund();
        copurchasing.deleteParticipation(participation);

        participationRepository.delete(participation);
    }
}
