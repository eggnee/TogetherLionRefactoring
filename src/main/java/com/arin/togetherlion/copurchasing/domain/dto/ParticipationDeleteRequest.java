package com.arin.togetherlion.copurchasing.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipationDeleteRequest {
    @NotNull(message = "공동구매 참여 Id는 필수 입력 값입니다.")
    Long participationId;

    @NotNull(message = "참여자 Id는 필수 입력 값입니다.")
    Long deleterId;
}
