package org.zzin.splitfy.domain.event.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.EventNumber;

public interface EventNumberRepository extends JpaRepository<EventNumber, Long> {

    List<EventNumber> findByEventIdOrderByNumberAsc(Long eventId);

}
