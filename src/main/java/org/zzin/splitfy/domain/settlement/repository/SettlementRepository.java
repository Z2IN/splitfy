package org.zzin.splitfy.domain.settlement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.settlement.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

}
