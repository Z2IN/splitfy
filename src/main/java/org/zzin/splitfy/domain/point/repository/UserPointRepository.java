package org.zzin.splitfy.domain.point.repository;

import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.point.entity.UserPoint;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserPointRepository {

  private final UserPointJPARepository userPointJPARepository;

  public UserPoint save(UserPoint userPoint) {
    return userPointJPARepository.save(userPoint);
  }
}
