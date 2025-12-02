package org.zzin.splitfy.domain.transaction.dto;

import org.jspecify.annotations.NullMarked;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@NullMarked
public class CreateDepositTransactionParamDTO {

  private final String transactionUUID;
  private final long userId;
  private final long amount;
  private final long beforePoint;
  private final long afterPoint;
}
