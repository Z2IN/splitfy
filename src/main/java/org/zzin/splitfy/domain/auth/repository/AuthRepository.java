package org.zzin.splitfy.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.auth.entity.User;

public interface AuthRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  List<User> findByIdIn(Set<Long> ids);
}
