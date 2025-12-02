package org.zzin.splitfy.domain.point.Service;

import org.springframework.stereotype.Service;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

  private final AuthInnerService authInnerService;

  public long getPointBy(long userId) {
    return authInnerService.getPointBy(userId);
  }
}
