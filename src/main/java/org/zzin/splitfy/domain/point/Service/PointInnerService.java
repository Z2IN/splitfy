package org.zzin.splitfy.domain.point.Service;

public interface PointInnerService {

  long initUserPoint(long userId);

  // TODO: eventId + 운영자 가 userId 에게 reward를 보내면 됩니다
  // 트랜잭션에도 저장해야할까요?
  void sendEventRewardPoint(long userId, long reward);

}
