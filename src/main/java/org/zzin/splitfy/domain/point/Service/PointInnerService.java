package org.zzin.splitfy.domain.point.Service;

public interface PointInnerService {

  long initUserPoint(long userId);

  default void sendEventRewardPoint(long eventId, long userId, long reward) {

    // TODO: eventId + 운영자 가 userId 에게 reward를 보내면 됩니다
    // 트랜잭션에도 저장해야할까요?

  }

}
