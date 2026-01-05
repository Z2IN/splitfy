package org.zzin.splitfy.domain.settlement.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.point.service.PointInnerService;
import org.zzin.splitfy.domain.settlement.model.UserBalance;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementTransferService {

  private final PointInnerService pointInnerService;
  private final SettlementStatusService settlementStatusService;

  /**
   * netBalance를 기반으로 실제 이체를 수행하는 메서드
   *
   * @param netBalance 사용자별 정산 금액 (양수: 받을 금액, 음수: 줘야 할 금액)
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeTransfers(Map<Long, Long> netBalance, long settlementId) {
    Deque<UserBalance> creditors = new ArrayDeque<>();  // 결제한 사람 (양수)
    Deque<UserBalance> debtors = new ArrayDeque<>();    // 이체할 사람 (음수)

    netBalance.forEach((userId, balance) -> {
      if (balance > 0) {
        creditors.add(new UserBalance(userId, balance));
      } else if (balance < 0) {
        debtors.add(new UserBalance(userId, -balance));
      }
    });

    // 큐 기반 그리디 매칭으로 이체를 수행
    while (!debtors.isEmpty() && !creditors.isEmpty()) {
      UserBalance debtor = debtors.peek();
      UserBalance creditor = creditors.peek();

      long transferAmount = Math.min(debtor.getRemaining(), creditor.getRemaining());
      pointInnerService.transferPoint(debtor.getUserId(), creditor.getUserId(), transferAmount);

      debtor.minusRemaining(transferAmount);
      creditor.minusRemaining(transferAmount);
      if (debtor.isRemainingZero()) {
        debtors.poll();
      }
      if (creditor.isRemainingZero()) {
        creditors.poll();
      }
    }

    settlementStatusService.updateSettlementStatus(settlementId, true);
  }
}
