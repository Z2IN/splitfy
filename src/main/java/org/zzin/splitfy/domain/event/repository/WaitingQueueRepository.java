package org.zzin.splitfy.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;

public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

}
