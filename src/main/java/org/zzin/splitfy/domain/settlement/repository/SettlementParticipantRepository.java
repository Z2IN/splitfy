package org.zzin.splitfy.domain.settlement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.settlement.entity.SettlementParticipant;

public interface SettlementParticipantRepository extends JpaRepository<SettlementParticipant, Long> {

  List<SettlementParticipant> findBySettlementIdIn(List<Long> settlementIds);
}