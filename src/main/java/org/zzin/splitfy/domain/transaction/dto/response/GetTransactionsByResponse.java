package org.zzin.splitfy.domain.transaction.dto.response;

import java.time.LocalDateTime;

import org.zzin.splitfy.domain.transaction.dto.TransactionDetailDTO;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;

public record GetTransactionsByResponse(long transactionId, long amount, TransactionType type,
                                        long beforePoint, long afterPoint,
                                        LocalDateTime transactionTime) {

  public static GetTransactionsByResponse fromDto(TransactionDetailDTO dto) {
    return new GetTransactionsByResponse(
        dto.getTransactionId(),
        dto.getAmount(),
        dto.getType(),
        dto.getBeforePoint(),
        dto.getAfterPoint(),
        dto.getTransactionTime()
    );
  }
}
