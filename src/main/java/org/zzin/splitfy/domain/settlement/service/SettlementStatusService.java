package org.zzin.splitfy.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementStatusService {

  private final SettlementRepository settlementRepository;

  /**
   * 이체 성공/실패에 따라 settlement 상태를 업데이트합니다.
   *
   * @param settlementId Settlement ID
   * @param isSuccess    이체 성공 여부
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSettlementStatus(Long settlementId, boolean isSuccess) {
    Settlement settlement = settlementRepository.findById(settlementId)
        .orElseThrow(() -> new BusinessException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

    if (isSuccess) {
      settlement.markAsSucceeded();
    } else {
      settlement.markAsFailed();
    }
  }
}
