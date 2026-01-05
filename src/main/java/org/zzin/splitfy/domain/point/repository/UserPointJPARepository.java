package org.zzin.splitfy.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.point.entity.UserPoint;

public interface UserPointJPARepository extends JpaRepository<UserPoint, Long> {

}
