package org.zzin.splitfy.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.EventEntry;

public interface EventEntryRepository extends JpaRepository<EventEntry, Long> {

}
