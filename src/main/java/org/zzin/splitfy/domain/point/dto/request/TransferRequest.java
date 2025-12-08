package org.zzin.splitfy.domain.point.dto.request;

import org.jspecify.annotations.NonNull;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
    @NotNull(message = "받는 사람 ID는 필수입니다.") @NonNull Long toUserId,
    @NotNull(message = "금액은 필수입니다.") @Positive(message = "금액은 양수여야 합니다.") @NonNull Long amount
) {

}
