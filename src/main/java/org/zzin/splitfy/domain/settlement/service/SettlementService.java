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
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementService {

  private final SettlementRepository settlementRepository;

  @Transactional
  public SettlementResponse createSettlement(AuthUser authUser, SettlementRequest request) {
    long issuerId = authUser.userId();
    List<PaymentRequest> paymentRequests = request.payments();

    // 비즈니스 규칙 검증
    validatePaymentRequests(paymentRequests);

    // 총 지불 금액 / 정산 금액 계산
    Map<Long, Long> contributed = new HashMap<>();
    Map<Long, Long> allocated = new HashMap<>();

    for (PaymentRequest paymentRequest : paymentRequests) {
      long paidAmount = paymentRequest.paidAmount();
      long payerId = paymentRequest.payerId();
      List<Long> allocationIds = paymentRequest.allocationIds();

      // 사용자별 지불 금액 누적
      contributed.merge(payerId, paidAmount, Long::sum);

      // 정산 금액 계산
      int count = allocationIds.size();    // 정산 대상자 수
      long eachShare = paidAmount / count; // 1인당 정산 금액
      long remainder = paidAmount % count; // 회사가 부담하는 금액

      // 참여자들에게 eachShare만 부과
      for (Long userId : allocationIds) {
        allocated.merge(userId, eachShare, Long::sum); // 각 사용자의 정산액 누적
      }
    }

    // totalAmount 계산
    long totalAmount = allocated.values().stream().mapToLong(Long::longValue).sum();

    // Settlement 생성 및 저장
    Settlement settlement = new Settlement(issuerId, totalAmount);
    settlementRepository.save(settlement);

    // Payment 엔티티 생성 및 저장
    for (PaymentRequest paymentRequest : paymentRequests) {
      settlement.addPayment(
          paymentRequest.paidAmount(),
          paymentRequest.payerId(),
          paymentRequest.title()
      );
    }

    // 최소 정산 알고리즘: 사용자별 정산 금액 계산
    Map<Long, Long> netBalance = calculateNetBalance(contributed, allocated);

    // SettlementParticipant 저장 (각 사용자의 정산 금액)
    for (Map.Entry<Long, Long> entry : netBalance.entrySet()) {
      settlement.addParticipant(entry.getKey(), entry.getValue());
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

    // 모든 참여자의 정산 금액 계산
    for (Long userId : allocated.keySet()) {
      long userAllocated = allocated.getOrDefault(userId, 0L);
      long userContributed = contributed.getOrDefault(userId, 0L);
      long balance = userContributed - userAllocated; // 양수: 받을 금액, 음수: 송금 할 금액

      if (balance != 0) {
        netBalance.put(userId, balance);
      }
    }

    // 기여만 하고 부담이 없는 사용자 처리
    for (Long userId : contributed.keySet()) {
      if (!allocated.containsKey(userId)) {
        long userContributed = contributed.get(userId);
        netBalance.put(userId, userContributed); // 전액 받을 금액
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

      // 결제자가 정산 대상자에 포함되어 있는지 검증
      if (!allocationIds.contains(paymentRequest.payerId())) {
        throw new BusinessException(SettlementErrorCode.SELF_ALLOCATION_NOT_ALLOWED);
      }
    }
  }
}
