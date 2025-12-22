package org.zzin.splitfy.domain.settlement.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.point.service.PointInnerService;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementTransferService {

  private final PointInnerService pointInnerService;

  /**
   * netBalance를 기반으로 실제 이체를 수행하는 메서드
   *
   * @param netBalance 사용자별 정산 금액 (양수: 받을 금액, 음수: 줘야 할 금액)
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeTransfers(Map<Long, Long> netBalance) {
    Map<Long, Long> creditors = new HashMap<>();  // 결제한 사람 (양수)
    Map<Long, Long> debtors = new HashMap<>();    // 이체할 사람 (음수)

    for (Map.Entry<Long, Long> entry : netBalance.entrySet()) {
      long userId = entry.getKey();
      long balance = entry.getValue();

      if (balance > 0) {
        creditors.put(userId, balance);
      } else if (balance < 0) {
        debtors.put(userId, -balance); // 절댓값으로 저장
      }
    }

    // 이체
    for (Map.Entry<Long, Long> debtorEntry : debtors.entrySet()) {
      long debtorId = debtorEntry.getKey();
      long remainingDebt = debtorEntry.getValue();

      for (Map.Entry<Long, Long> creditorEntry : creditors.entrySet()) {
        if (remainingDebt <= 0) {
          break;
        }

        long creditorId = creditorEntry.getKey();
        long remainingCredit = creditorEntry.getValue();

        if (remainingCredit <= 0) {
          continue;
        }

        // 이체할 금액 계산
        long transferAmount = Math.min(remainingDebt, remainingCredit);

        // 실제 이체 실행: debtorId가 creditorId에게 transferAmount만큼 이체
        pointInnerService.transferPoint(debtorId, creditorId, transferAmount);
        // 잔액 업데이트
        remainingDebt -= transferAmount;
        creditorEntry.setValue(remainingCredit - transferAmount);
      }
    }
  }
}
