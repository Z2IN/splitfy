package org.zzin.splitfy.domain.transaction.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
// DataJpaTest already wraps tests in transactions and rolls back; explicit @Transactional is not
// required
import org.zzin.splitfy.common.config.JPAuditingConfig;
import org.zzin.splitfy.common.config.QueryDSLConfig;
import org.zzin.splitfy.domain.transaction.dto.TransactionDetailDTO;
import org.zzin.splitfy.domain.transaction.entity.Transaction;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;

import java.util.List;

import org.springframework.data.domain.Page;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
    QueryDSLConfig.class,
    JPAuditingConfig.class,
    TransactionQueryRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TransactionQueryRepositoryTest {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private TransactionQueryRepository transactionQueryRepository;

  @Test
  void getTransactionsByUserId_사용자_거래목록_조회_성공() throws Exception {
    Transaction t1 = Transaction.builder()
        .userId(1L)
        .amount(100L)
        .type(TransactionType.DEPOSIT)
        .beforePoint(1000L)
        .afterPoint(1100L)
        .uuid("uuid-1")
        .build();

    Transaction t2 = Transaction.builder()
        .userId(1L)
        .amount(200L)
        .type(TransactionType.TRANSFER_OUT)
        .beforePoint(1100L)
        .afterPoint(900L)
        .uuid("uuid-2")
        .build();

    Transaction t3 = Transaction.builder()
        .userId(1L)
        .amount(50L)
        .type(TransactionType.DEPOSIT)
        .beforePoint(900L)
        .afterPoint(950L)
        .uuid("uuid-3")
        .build();

    Transaction other = Transaction.builder()
        .userId(2L)
        .amount(999L)
        .type(TransactionType.DEPOSIT)
        .beforePoint(0L)
        .afterPoint(999L)
        .uuid("uuid-other")
        .build();

    transactionRepository.saveAndFlush(t1);
    Thread.sleep(10);
    transactionRepository.saveAndFlush(t2);
    Thread.sleep(10);
    transactionRepository.saveAndFlush(t3);
    transactionRepository.saveAndFlush(other);

    Page<TransactionDetailDTO> page0 = transactionQueryRepository.getTransactionsByUserId(1L, 0, 2);

    assertThat(page0.getTotalElements()).isEqualTo(3);
    List<TransactionDetailDTO> content0 = page0.getContent();
    assertThat(content0).hasSize(2);

    assertThat(content0.get(0).getAmount()).isEqualTo(50L);
    assertThat(content0.get(1).getAmount()).isEqualTo(200L);
    assertThat(content0.get(0).getTransactionTime()).isNotNull();

    Page<TransactionDetailDTO> page1 = transactionQueryRepository.getTransactionsByUserId(1L, 1, 2);
    assertThat(page1.getTotalElements()).isEqualTo(3);
    assertThat(page1.getContent()).hasSize(1);
    assertThat(page1.getContent().get(0).getAmount()).isEqualTo(100L);
  }

}
