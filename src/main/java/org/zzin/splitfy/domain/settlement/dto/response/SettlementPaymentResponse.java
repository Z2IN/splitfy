package org.zzin.splitfy.domain.settlement.dto.response;

import java.util.List;

public record SettlementPaymentResponse(
    String title,
    long paidAmount,
    String payerName,
    List<String> allocationNames
) {

}
