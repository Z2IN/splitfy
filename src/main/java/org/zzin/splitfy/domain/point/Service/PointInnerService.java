package org.zzin.splitfy.domain.point.Service;

public interface PointInnerService {

  long initUserPoint(long userId);

  void sendEventRewardPoint(long userId, long reward);

}
