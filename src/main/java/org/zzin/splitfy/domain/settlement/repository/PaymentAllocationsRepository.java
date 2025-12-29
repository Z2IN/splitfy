package org.zzin.splitfy.domain.settlement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.settlement.entity.PaymentAllocations;

public interface PaymentAllocationsRepository extends JpaRepository<PaymentAllocations, Long> {

  List<PaymentAllocations> findByPaymentIdIn(List<Long> paymentIds);
}