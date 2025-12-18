package org.zzin.splitfy.domain.point.Service;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.point.entity.UserPoint;
import org.zzin.splitfy.domain.point.repository.UserPointRepository;

import lombok.RequiredArgsConstructor;

@NullMarked
@Service
@RequiredArgsConstructor
public class DefaultPointInnerService implements PointInnerService {

  private final UserPointRepository userPointRepository;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public long initUserPoint(long userId) {
    UserPoint userPoint = new UserPoint(userId);
    return userPointRepository.save(userPoint).getPoint();
  }

}
