package org.zzin.splitfy.domain.settlement.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.point.Service.PointService;
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.entity.Payment;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.entity.SettlementParticipant;
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;
import org.zzin.splitfy.domain.settlement.repository.PaymentRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementParticipantRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementService {

  private final SettlementRepository settlementRepository;
  private final PaymentRepository paymentRepository;
  private final SettlementParticipantRepository settlementParticipantRepository;
  private final PointService pointService;

  @Transactional
  public SettlementResponse createSettlement(AuthUser authUser, SettlementRequest request) {
    long issuerId = authUser.userId();
    List<PaymentRequest> paymentRequests = request.payments();

    // 비즈니스 규칙 검증
    validatePaymentRequests(paymentRequests);

    // 총 지불 금액 / 정산 금액 계산
    Map<Long, Long> contributed = new HashMap<>();
    Map<Long, Long> allocated = new HashMap<>();
    long totalRemainder = 0L;

    for (PaymentRequest paymentRequest : paymentRequests) {
      long paidAmount = paymentRequest.paidAmount();
      long payerId = paymentRequest.payerId();
      List<Long> allocationIds = paymentRequest.allocationIds();

      // 정산 금액 계산
      int count = allocationIds.size();    // 정산 대상자 수
      long eachShare = paidAmount / count; // 1인당 정산 금액
      long remainder = paidAmount % count; // 나머지 금액 (payerId가 가져감)
      totalRemainder += remainder;         // 총 나머지 금액 누적

      // 사용자별 지불 금액 누적 (전액)
      contributed.merge(payerId, paidAmount, Long::sum);

      // 참여자들에게 eachShare만 부과 (remainder는 payerId가 가져감)
      for (Long userId : allocationIds) {
        allocated.merge(userId, eachShare, Long::sum);
      }
    }

    // totalAmount 계산
    long totalAmount = allocated.values().stream().mapToLong(Long::longValue).sum();

    // Settlement 생성 및 저장
    Settlement settlement = new Settlement(issuerId, totalAmount, totalRemainder);
    settlement = settlementRepository.save(settlement);

    // Payment 엔티티 생성 및 저장
    for (PaymentRequest paymentRequest : paymentRequests) {
      Payment payment = settlement.createPayment(
          paymentRequest.paidAmount(),
          paymentRequest.payerId(),
          paymentRequest.title()
      );
      paymentRepository.save(payment);
    }

    // 최소 정산 알고리즘: 사용자별 정산 금액 계산
    Map<Long, Long> netBalance = calculateNetBalance(contributed, allocated);

    // SettlementParticipant 저장 (각 사용자의 정산 금액)
    for (Map.Entry<Long, Long> entry : netBalance.entrySet()) {
      SettlementParticipant participant = settlement.createParticipant(
          entry.getKey(),
          entry.getValue()
      );
      settlementParticipantRepository.save(participant);
    }

    // 자동 이체 실행 및 상태 업데이트
    try {
      executeTransfers(netBalance);
      settlement.markAsSucceeded();
    } catch (Exception e) {
      settlement.markAsFailed();
      throw e; // 예외를 다시 던져서 트랜잭션 롤백
    }

    return new SettlementResponse(settlement.getId());
  }

  /**
   * 최소 정산 알고리즘: 사용자별 정산 금액 계산
   *
   * @param contributed 사용자별 기여금 (실제로 낸 금액)
   * @param allocated   사용자별 부담금 (부담해야 하는 금액)
   * @return 사용자별 정산 금액 (양수: 받을 금액, 음수: 줘야 할 금액)
   */
  private Map<Long, Long> calculateNetBalance(
      Map<Long, Long> contributed,
      Map<Long, Long> allocated
  ) {
    Map<Long, Long> netBalance = new HashMap<>();

    // contributed와 allocated를 모두 고려하여 모든 사용자의 정산 금액 계산
    Set<Long> allUserIds = new HashSet<>();
    allUserIds.addAll(contributed.keySet());
    allUserIds.addAll(allocated.keySet());

    for (Long userId : allUserIds) {
      long userContributed = contributed.getOrDefault(userId, 0L);
      long userAllocated = allocated.getOrDefault(userId, 0L);
      long balance = userContributed - userAllocated; // 양수: 받을 금액, 음수: 송금 할 금액

      if (balance != 0) {
        netBalance.put(userId, balance);
      }
    }

    return netBalance;
  }

  /**
   * 비즈니스 규칙 검증
   */
  private void validatePaymentRequests(List<PaymentRequest> paymentRequests) {
    for (PaymentRequest paymentRequest : paymentRequests) {
      List<Long> allocationIds = paymentRequest.allocationIds();

      // 중복 검증
      Set<Long> uniqueIds = new HashSet<>(allocationIds);
      if (uniqueIds.size() != allocationIds.size()) {
        throw new BusinessException(SettlementErrorCode.DUPLICATE_ALLOCATION_TARGETS);
      }
    }
  }

  /**
   * netBalance를 기반으로 실제 이체를 수행하는 메서드
   *
   * @param netBalance 사용자별 정산 금액 (양수: 받을 금액, 음수: 줘야 할 금액)
   */
  private void executeTransfers(Map<Long, Long> netBalance) {
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
        AuthUser debtorAuthUser = new AuthUser(debtorId);
        pointService.transferTo(creditorId, transferAmount, debtorAuthUser);

        // 잔액 업데이트
        remainingDebt -= transferAmount;
        creditorEntry.setValue(remainingCredit - transferAmount);
      }
    }
  }
}
