package org.zzin.splitfy.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private long point = 0L;

  @Column(nullable = false, unique = true)
  private String username;

  @NullMarked
  public static User ofSignup(String email,
      String username,
      String encodedPassword) {
    User user = new User();
    user.email = email;
    user.username = username;
    user.password = encodedPassword;
    user.point = 0L;
    return user;
  }

  // TODO 삭제해줘.
  @Deprecated
  public void addPoint(long amount) {
    if (amount < 0) {
      throw new BusinessException(AuthErrorCode.INVALID_POINT_BALANCE);
    }
    if (Long.MAX_VALUE - this.point < amount) {
      throw new BusinessException(AuthErrorCode.INVALID_POINT_BALANCE);
    }
    this.point += amount;
  }

  public void deductPoint(long amount) {
    if (amount < 0) {
      throw new BusinessException(AuthErrorCode.INVALID_POINT_BALANCE);
    }
    if (this.point < amount) {
      throw new BusinessException(AuthErrorCode.INSUFFICIENT_POINT_BALANCE);
    }
    this.point -= amount;
  }
}
