package org.zzin.splitfy.domain.point.repository;

import org.springframework.stereotype.Repository;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.entity.QUser;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.model.UserPoint;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointQRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public boolean isUserExistBy(long userId) {
    QUser user = QUser.user;

    Integer fetchOne = jpaQueryFactory
        .selectOne()
        .from(user)
        .where(user.id.eq(userId))
        .fetchFirst();

    return fetchOne != null;
  }

  public UserPoint getUserPointBy(long userId) {
    QUser user = QUser.user;

    UserPoint point = jpaQueryFactory
        .select(Projections.constructor(UserPoint.class,
            user.point
        ))
        .from(user)
        .where(user.id.eq(userId))
        .fetchOne();

    if (point == null) {
      throw new BusinessException(PointErrorCode.USER_NOT_FOUND);
    }

    return point;
  }

}
