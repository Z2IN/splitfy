package org.zzin.splitfy.domain.point.Service;

import org.springframework.stereotype.Service;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

  private final AuthInnerService authInnerService;

  /**
   * 주어진 사용자 ID에 대한 포인트 잔액을 조회하여 반환합니다.
   *
   * @param userId 조회할 사용자의 고유 식별자
   * @return 요청한 사용자의 현재 포인트 잔액
   */
  public long getPointBy(long userId) {
    return authInnerService.getPointBy(userId);
  }

}
