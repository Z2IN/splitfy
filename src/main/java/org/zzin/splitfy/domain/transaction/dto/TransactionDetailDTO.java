package org.zzin.splitfy.domain.transaction.dto;

import java.time.LocalDateTime;

import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@NullMarked
public class TransactionDetailDTO {

  private final long transactionId;
  private final long amount;
  private final TransactionType type;
  private final long beforePoint;
  private final long afterPoint;
  private final LocalDateTime transactionTime;
}
