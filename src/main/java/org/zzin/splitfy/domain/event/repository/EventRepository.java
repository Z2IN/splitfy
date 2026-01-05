package org.zzin.splitfy.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
