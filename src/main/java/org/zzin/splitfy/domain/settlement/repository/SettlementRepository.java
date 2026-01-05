package org.zzin.splitfy.domain.settlement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zzin.splitfy.domain.settlement.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  @Query("""
      select s
      from Settlement s
      where exists (
            select 1 
            from SettlementParticipant  sp
            where sp.settlementId = s.id
            and sp.participantId = :userId
            )
      """)
  Page<Settlement> findByParticipantUserId(
      @Param("userId") Long userId,
      Pageable pageable
  );
}
