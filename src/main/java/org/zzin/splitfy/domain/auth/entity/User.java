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
  private Long point = 0L;

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
}
