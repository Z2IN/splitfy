package org.zzin.splitfy.domain.point.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositRequest(
    @NotNull(message = "amount는 필수값입니다.") @Positive(message = "amount는 양수여야 합니다.") Long point
) {

}
