package org.zzin.splitfy.domain.event.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;

public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

  boolean existsByEventIdAndUserId(long eventId, long userId);

  Optional<WaitingQueue> findByEventIdAndUserId(long eventId, long userId);
}
