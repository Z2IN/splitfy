package org.zzin.splitfy.domain.settlement.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.jspecify.annotations.Nullable;

public record PaymentAllocationDto(
    Long paymentId,
    Long settlementId,
    Long paidAmount,
    Long payerId,
    Long shareAmount,
    String title,
    @Nullable Long allocationUserId
) {

  @QueryProjection
  public PaymentAllocationDto {
  }
}