package org.zzin.splitfy.domain.point.Service;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PointInnerService {

  long initUserPoint(long userId);

  /**
   * 송신자로부터 수신자에게 포인트를 이체합니다. (자동 이체용)
   *
   * @param fromUserId 송신자의 사용자 ID
   * @param toUserId   수신자의 사용자 ID
   * @param amount     이체할 포인트 양 (양수여야 함)
   */
  void transferPoint(long fromUserId, long toUserId, long amount);
  void sendEventRewardPoint(long userId, long reward);

}
