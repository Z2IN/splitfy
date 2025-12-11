package org.zzin.splitfy.domain.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.transaction.dto.TransactionDetailDTO;
import org.zzin.splitfy.domain.transaction.repository.TransactionQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionQueryRepository transactionQueryRepository;

  /**
   * 사용자의 거래 내역을 페이지 단위로 조회합니다.
   *
   * @param userId 조회 대상 사용자 ID
   * @param page   0부터 시작하는 페이지 인덱스(예: 첫 페이지는 0)
   * @param size   페이지당 조회할 레코드 수
   * @return 거래 상세 정보의 페이지(Page<TransactionDetailDTO>). 빈 페이지가 될 수 있습니다.
   * @see TransactionQueryRepository#getTransactionsByUserId(long, int, int)
   */
  @Transactional(readOnly = true)
  public Page<TransactionDetailDTO> getTransactionsByUserId(long userId, int page, int size) {
    return transactionQueryRepository.getTransactionsByUserId(userId, page, size);
  }
}
