package org.zzin.splitfy.domain.settlement.repository;

import static org.zzin.splitfy.domain.settlement.entity.QPayment.payment;
import static org.zzin.splitfy.domain.settlement.entity.QPaymentAllocations.paymentAllocations;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.settlement.dto.PaymentAllocationDto;
import org.zzin.splitfy.domain.settlement.dto.QPaymentAllocationDto;

@Repository
@RequiredArgsConstructor
@NullMarked
public class SettlementQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<PaymentAllocationDto> findPaymentAllocationsInSettlements(List<Long> settlementIds) {
    return queryFactory
        .select(new QPaymentAllocationDto(
            payment.id,
            payment.settlementId,
            payment.paidAmount,
            payment.payerId,
            payment.shareAmount,
            payment.title,
            paymentAllocations.userId
        ))
        .from(payment)
        .leftJoin(paymentAllocations).on(paymentAllocations.paymentId.eq(payment.id))
        .where(payment.settlementId.in(settlementIds))
        .orderBy(
            payment.id.asc(),
            paymentAllocations.userId.asc()
        )
        .fetch();
  }
}