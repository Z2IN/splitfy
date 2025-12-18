package org.zzin.splitfy.domain.point.entity;

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
}
