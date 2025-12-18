package org.zzin.splitfy.domain.point.entity;

import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_points")
public class UserPoint {

  @Id
  private Long userId;

  @Column(nullable = false)
  private long point = 0L;

  public UserPoint(long userId) {
    this.userId = userId;
  }

  @NullMarked
  public void addPoint(long amount) {
    if (amount < 0) {
      throw new BusinessException(PointErrorCode.INVALID_POINT_BALANCE);
    }
    if (Long.MAX_VALUE - this.point < amount) {
      throw new BusinessException(PointErrorCode.INVALID_POINT_BALANCE);
    }
    this.point += amount;
  }
}
