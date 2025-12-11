package org.zzin.splitfy.domain.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.transaction.dto.TransactionDetailDTO;
import org.zzin.splitfy.domain.transaction.entity.QTransaction;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public Page<TransactionDetailDTO> getTransactionsByUserId(long userId, int page, int size) {
    QTransaction transaction = QTransaction.transaction;

    var content = jpaQueryFactory
        .select(Projections.constructor(TransactionDetailDTO.class,
            transaction.id,
            transaction.amount,
            transaction.type,
            transaction.beforePoint,
            transaction.afterPoint,
            transaction.transactionTime))
        .from(transaction)
        .where(transaction.userId.eq(userId))
        .orderBy(transaction.transactionTime.desc())
        .offset((long) page * size)
        .limit(size)
        .fetch();

    var total = jpaQueryFactory
        .select(transaction.count())
        .from(transaction)
        .where(transaction.userId.eq(userId));

    return PageableExecutionUtils.getPage(content, PageRequest.of(page, size), total::fetchOne);
  }
}
