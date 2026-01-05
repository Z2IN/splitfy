package org.zzin.splitfy.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzin.splitfy.domain.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
