package org.zzin.splitfy.domain.auth.service;

import org.jspecify.annotations.NonNull;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;

public interface AuthInnerService {

  default long getPointBy(long userId) {
    // TODO: 사용자의 실제 포인트를 반환해야함.
    // 예외 처리도 필요함.
    return 0L;
  }

  default @NonNull PointChangeResultDTO addPoint(long userId, long amount) {
    // TODO: 사용자의 포인트를 실제로 증가시켜야함.
    // 예외 처리도 필요함.
    return new PointChangeResultDTO(0, 0);
  }

  default @NonNull PointTransferSummaryDTO transferPoint(long senderId, long receiverId,
      long amount) {
    // TODO: 포인트 송금 로직 구현.
    // 검증 및 예외 처리 필요.
    return new PointTransferSummaryDTO(0, 0, 0, 0);
  }
}
