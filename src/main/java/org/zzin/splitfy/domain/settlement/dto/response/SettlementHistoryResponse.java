package org.zzin.splitfy.domain.settlement.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.zzin.splitfy.domain.settlement.enums.SettlementStatus;

public record SettlementHistoryResponse(
    Long settlementId,
    Long totalPaidAmount,
    SettlementStatus status,
    LocalDateTime issuedAt,
    LocalDateTime succeededAt,
    Long settlementAmount,
    List<SettlementPaymentResponse> payments
) {

}
