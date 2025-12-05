package org.zzin.splitfy.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.auth.entity.User;

public interface AuthRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
