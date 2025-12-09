package org.zzin.splitfy.domain.auth.service;

import lombok.NonNull;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;

public interface AuthInnerService {

  long getPointBy(long userId);

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
