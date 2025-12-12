package org.zzin.splitfy.domain.settlement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SettlementRequest(
    @NotEmpty
    @Valid
    List<PaymentRequest> payments
) {

}
