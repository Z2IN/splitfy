package org.zzin.splitfy.domain.auth.service;

import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;

@NullMarked
public interface AuthInnerService {

  long getPointBy(long userId);

  @NonNull
  PointChangeResultDTO addPoint(long userId, long amount);

  default @NonNull PointTransferSummaryDTO transferPoint(long senderId, long receiverId,
      long amount) {
    // TODO: 포인트 송금 로직 구현.
    // 검증 및 예외 처리 필요.
    return new PointTransferSummaryDTO(0, 0, 0, 0);
  }
}
