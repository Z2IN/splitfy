package org.zzin.splitfy.domain.transaction.service;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.entity.Transaction;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;
import org.zzin.splitfy.domain.transaction.exception.TransactionErrorCode;
import org.zzin.splitfy.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@NullMarked
@Slf4j
public class DefaultTransactionInnerService implements TransactionInnerService {

  private final TransactionRepository transactionRepository;

  private void createTransaction(TransactionInfoDTO param, TransactionType type) {
    Transaction transaction = Transaction.builder()
        .uuid(param.getTransactionUUID())
        .userId(param.getUserId())
        .type(type)
        .amount(param.getAmount())
        .beforePoint(param.getBeforePoint())
        .afterPoint(param.getAfterPoint())
        .build();

    try {
      transactionRepository.save(transaction);
    } catch (Exception e) {
      log.warn("Failed to create deposit transaction: {}", e.getMessage(), e);
      throw new BusinessException(TransactionErrorCode.TRANSACTION_CREATION_FAILED);
    }
  }

  @Override
  @Transactional
  public void createDepositTransaction(TransactionInfoDTO param) {
    createTransaction(param, TransactionType.DEPOSIT);
  }

  @Override
  @Transactional
  public void createTransferInTransaction(TransactionInfoDTO param) {
    createTransaction(param, TransactionType.TRANSFER_IN);
  }

  @Override
  @Transactional
  public void createTransferOutTransaction(TransactionInfoDTO param) {
    createTransaction(param, TransactionType.TRANSFER_OUT);
  }
}
