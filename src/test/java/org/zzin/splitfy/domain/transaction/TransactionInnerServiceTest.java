package org.zzin.splitfy.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.transaction.dto.CreateDepositTransactionParamDTO;
import org.zzin.splitfy.domain.transaction.entity.Transaction;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;
import org.zzin.splitfy.domain.transaction.exception.TransactionErrorCode;
import org.zzin.splitfy.domain.transaction.repository.TransactionRepository;
import org.zzin.splitfy.domain.transaction.service.DefaultTransactionInnerService;

@ExtendWith(MockitoExtension.class)
class TransactionInnerServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private DefaultTransactionInnerService service;

  @Test
  void createDepositTransaction_저장_성공() {
    CreateDepositTransactionParamDTO param = CreateDepositTransactionParamDTO.builder()
        .transactionUUID("uuid-123")
        .userId(1L)
        .amount(100L)
        .beforePoint(500L)
        .afterPoint(600L)
        .build();

    service.createDepositTransaction(param);

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
    then(transactionRepository).should().save(captor.capture());

    Transaction saved = captor.getValue();
    assertThat(saved).isNotNull();
    assertThat(saved.getUuid()).isEqualTo("uuid-123");
    assertThat(saved.getUserId()).isEqualTo(1L);
    assertThat(saved.getAmount()).isEqualTo(100L);
    assertThat(saved.getBeforePoint()).isEqualTo(500L);
    assertThat(saved.getAfterPoint()).isEqualTo(600L);
    assertThat(saved.getType()).isEqualTo(TransactionType.DEPOSIT);
  }

  @Test
  void createDepositTransaction_db_저장_실패시_BusinessException_발생() {
    CreateDepositTransactionParamDTO param = CreateDepositTransactionParamDTO.builder()
        .transactionUUID("uuid-456")
        .userId(2L)
        .amount(200L)
        .beforePoint(1000L)
        .afterPoint(1200L)
        .build();

    given(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction.class)))
        .willThrow(new RuntimeException("db error"));

    assertThatThrownBy(() -> service.createDepositTransaction(param))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", TransactionErrorCode.TRANSACTION_CREATION_FAILED);
  }

}
