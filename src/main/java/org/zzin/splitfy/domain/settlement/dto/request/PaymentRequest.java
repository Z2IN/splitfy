package org.zzin.splitfy.domain.settlement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PaymentRequest(
    @NotBlank(message = "정산 제목을 입력해주세요.")
    String title,
    @Positive(message = "결제 금액이 0 이하일 수 없습니다.")
    long paidAmount,
    @Positive(message = "결제자 정보가 유효하지 않습니다.")
    long payerId,
    @NotEmpty(message = "정산 대상자가 존재하지 않습니다.")
    List<@Positive(message = "정산 대상자 중 유효하지 않은 사용자 ID가 포함되어 있습니다.") Long> allocationIds
) {

}
