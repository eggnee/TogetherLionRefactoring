package com.arin.togetherlion.copurchasing.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CopurchasingParticipateRequest {

    @NotBlank(message = "공동구매 게시물 Id는 필수 입력 값입니다.")
    private Long copurchasingId;

    @NotNull(message = "상품 구매 개수는 필수 입력 값입니다.")
    @Min(1)
    private int purchaseNumber;

    @NotBlank(message = "참여자 Id는 필수 입력 값입니다.")
    private Long participantId;
}
