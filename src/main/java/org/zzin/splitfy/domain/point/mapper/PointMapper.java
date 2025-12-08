package org.zzin.splitfy.domain.point.mapper;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;

@Component
@NullMarked
public class PointMapper {

  public TransactionInfoDTO toTransactionInfoDTO(
      String transactionUUID, long userId, long amount,
      UserPointChangeDetailDTO pointChangeDetail) {
    return TransactionInfoDTO.builder()
        .transactionUUID(transactionUUID)
        .userId(userId)
        .amount(amount)
        .beforePoint(pointChangeDetail.getBeforePoint())
        .afterPoint(pointChangeDetail.getAfterPoint())
        .build();
  }

}
