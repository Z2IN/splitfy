package org.zzin.splitfy.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.EventNumber;

public interface EventNumberRepository extends JpaRepository<EventNumber, Long> {

}
