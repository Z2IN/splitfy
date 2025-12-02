package org.zzin.splitfy.domain.auth.service;

public interface AuthInnerService {

  default long getPointBy(long userId) {
    // TODO: 사용자의 실제 포인트를 반환해야함.
    return 0L;
  }

}
