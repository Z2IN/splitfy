package org.zzin.splitfy.domain.settlement.dto;

import java.util.Map;

public record SettlementCalculationResultDto(long totalAmount, long totalRemainder,
                                             Map<Long, Long> netBalance) {
}
