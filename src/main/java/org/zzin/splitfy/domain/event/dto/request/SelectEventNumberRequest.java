package org.zzin.splitfy.domain.event.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectEventNumberRequest(
    @NotNull(message = "번호 선택은 필수값 입니다.")
    @Positive(message = "번호는 양수값 입니다.")
    Integer number
) {

}
