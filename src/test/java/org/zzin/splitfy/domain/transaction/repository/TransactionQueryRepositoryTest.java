package org.zzin.splitfy.domain.transaction.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
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
    // 사용자 1의 거래 3개를 생성 (순서/시간 비교를 위해 시간차를 둘 예정)
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

    // 다른 사용자의 거래를 하나 추가하여 조회 시 필터링이 올바르게 동작하는지 확인
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

    // 페이지 0(최신 2건) 조회: 사용자 1의 전체 거래는 3건이어야 함
    Page<TransactionDetailDTO> page0 = transactionQueryRepository.fetchTransactionsBy(1L, 0, 2);

    // 전체 건수 확인 (다른 사용자 거래는 제외)
    assertThat(page0.getTotalElements()).isEqualTo(3);

    List<TransactionDetailDTO> content0 = page0.getContent();

    // 페이지 사이즈만큼 반환되는지 확인
    assertThat(content0).hasSize(2);

    // 최신순 정렬 검증: 가장 최신(마지막 저장된 t3)이 첫 번째로 와야 함
    assertThat(content0.get(0).getAmount()).isEqualTo(50L);
    assertThat(content0.get(1).getAmount()).isEqualTo(200L);
    assertThat(content0.get(0).getTransactionTime()).isNotNull();

    // 페이지 1(나머지 1건) 조회: 두 번째 페이지에서 남은 t1(100L)이 반환되는지 확인
    Page<TransactionDetailDTO> page1 = transactionQueryRepository.fetchTransactionsBy(1L, 1, 2);
    assertThat(page1.getTotalElements()).isEqualTo(3);
    assertThat(page1.getContent()).hasSize(1);
    assertThat(page1.getContent().get(0).getAmount()).isEqualTo(100L);
  }

}
