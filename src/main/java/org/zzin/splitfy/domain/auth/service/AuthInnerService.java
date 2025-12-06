package org.zzin.splitfy.domain.auth.service;

import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;

public interface AuthInnerService {

  default long getPointBy(long userId) {
    // TODO: 사용자의 실제 포인트를 반환해야함.
    // 예외 처리도 필요함.
    return 0L;
  }

  default UserPointChangeDetailDTO addPoint(long userId, long amount) {
    // TODO: 사용자의 포인트를 실제로 증가시켜야함.
    // 예외 처리도 필요함.
    return new UserPointChangeDetailDTO(0, 0);
  }

}
