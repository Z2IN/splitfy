package org.zzin.splitfy.domain.point.dto.response;

import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;

public record TransferResponse(
    long amount,
    long beforePoint,
    long afterPoint
) {

  public static TransferResponse from(long amount, UserPointChangeDetailDTO pointChangeDetail) {
    return new TransferResponse(amount, pointChangeDetail.getBeforePoint(), pointChangeDetail
        .getAfterPoint());
  }
}
