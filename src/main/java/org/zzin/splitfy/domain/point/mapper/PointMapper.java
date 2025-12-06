package org.zzin.splitfy.domain.point.mapper;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;
import org.zzin.splitfy.domain.transaction.dto.CreateDepositTransactionParamDTO;

@Component
@NullMarked
public class PointMapper {

  public CreateDepositTransactionParamDTO toCreateDepositTransactionParamDTO(
      String transactionUUID, long userId, long amount,
      UserPointChangeDetailDTO pointChangeDetail) {
    return CreateDepositTransactionParamDTO.builder()
        .transactionUUID(transactionUUID)
        .userId(userId)
        .amount(amount)
        .beforePoint(pointChangeDetail.getBeforePoint())
        .afterPoint(pointChangeDetail.getAfterPoint())
        .build();
  }

}
