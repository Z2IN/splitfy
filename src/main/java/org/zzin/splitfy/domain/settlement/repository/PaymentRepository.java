package org.zzin.splitfy.domain.settlement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.settlement.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findBySettlementIdIn(List<Long> settlementIds);
}
