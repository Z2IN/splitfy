package org.zzin.splitfy.domain.settlement.service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.dto.CommonPage;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.settlement.dto.PaymentAllocationDto;
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementHistoryResponse;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementPaymentResponse;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.entity.SettlementParticipant;
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;
import org.zzin.splitfy.domain.settlement.repository.SettlementParticipantRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementQueryRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementService {

  private final SettlementTransferService settlementTransferService;
  private final SettlementStatusService settlementStatusService;
  private final SettlementRecordService settlementRecordService;
  private final SettlementRepository settlementRepository;
  private final SettlementQueryRepository settlementQueryRepository;
  private final AuthInnerService authInnerService;
  private final SettlementParticipantRepository settlementParticipantRepository;

  public SettlementResponse createSettlement(AuthUser authUser, SettlementRequest request) {
    long issuerId = authUser.userId();
    SettlementCalculation calculation = buildSettlementCalculation(request.payments());

    // 1. 정산 요청에 대한 settlement, payment 요청 기록
    Long settlementId = settlementRecordService.createSettlementRecord(
        issuerId,
        calculation.totalAmount(),
        calculation.totalRemainder(),
        request.payments(),
        calculation.netBalance()
    );

    // 2. 이체 실행
    try {
      settlementTransferService.executeTransfers(calculation.netBalance(), settlementId);
    } catch (Exception e) {
      // 3. 이체 실패 시 settlement state 변경
      settlementStatusService.updateSettlementStatus(settlementId, false);
    }

    return new SettlementResponse(settlementId);
  }

  private SettlementCalculation buildSettlementCalculation(List<PaymentRequest> paymentRequests) {
    validatePaymentRequests(paymentRequests);

    Map<Long, Long> contributed = new HashMap<>();
    Map<Long, Long> allocated = new HashMap<>();
    long totalRemainder = 0L;

    for (PaymentRequest paymentRequest : paymentRequests) {
      int allocationCount = paymentRequest.allocationIds().size();
      long paidAmount = paymentRequest.paidAmount();

      long eachShare = paidAmount / allocationCount;
      long remainder = paidAmount % allocationCount;
      totalRemainder += remainder;

      contributed.merge(paymentRequest.payerId(), paidAmount, Long::sum);
      paymentRequest.allocationIds()
          .forEach(userId -> allocated.merge(userId, eachShare, Long::sum));
    }

    long totalAmount = allocated.values().stream().mapToLong(Long::longValue).sum();
    Map<Long, Long> netBalance = calculateNetBalance(contributed, allocated);

    return new SettlementCalculation(totalAmount, totalRemainder, netBalance);
  }

  /**
   * 최소 정산 알고리즘: 사용자별 정산 금액 계산
   *
   * @param contributed 사용자별 기여금 (실제로 낸 금액)
   * @param allocated   사용자별 부담금 (부담해야 하는 금액)
   * @return 사용자별 정산 금액 (양수: 받을 금액, 음수: 줘야 할 금액)
   */
  private Map<Long, Long> calculateNetBalance(Map<Long, Long> contributed,
      Map<Long, Long> allocated) {
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

    return netBalance.isEmpty() ? Map.of() : Map.copyOf(netBalance);
  }

  @Transactional(readOnly = true)
  public CommonPage<SettlementHistoryResponse> getSettlementHistory(Pageable pageable,
      AuthUser authUser) {
    long userId = authUser.userId();

    // 1. 사용자가 참여한 정산들을 페이징 조회
    Page<Settlement> settlementPage = settlementRepository.findByParticipantUserId(userId,
        pageable);
    List<Settlement> settlements = settlementPage.getContent();

    if (settlements.isEmpty()) {
      return new CommonPage<>(List.of(), settlementPage.getTotalPages());
    }

    // 2. 정산 ID 목록 추출
    List<Long> settlementIds = settlements.stream().map(Settlement::getId).toList();

    // 3. 현재 사용자의 정산 금액 조회
    List<SettlementParticipant> participants = settlementParticipantRepository
        .findBySettlementIdIn(settlementIds);
    Map<Long, Long> settlementAmountMap = participants.stream()
        .filter(p -> p.getParticipantId() == userId)
        .collect(Collectors.toMap(
            SettlementParticipant::getSettlementId,
            SettlementParticipant::getSettlementAmount
        ));

    // 4. Repository를 통해 Payment와 PaymentAllocations를 한 번의 쿼리로 조회
    List<PaymentAllocationDto> dtos = settlementQueryRepository
        .findPaymentAllocationsInSettlements(settlementIds);

    // 5. User ID 수집
    Set<Long> userIds = new HashSet<>();
    for (PaymentAllocationDto dto : dtos) {
      userIds.add(dto.payerId());
      if (dto.allocationUserId() != null) {
        userIds.add(dto.allocationUserId());
      }
    }

    // 6. User 정보 조회
    Map<Long, String> userNameMap = authInnerService.findByIdIn(userIds);

    // 7. paymentId로 그룹화하여 SettlementPaymentResponse 생성 후 settlementId로 재그룹화
    Map<Long, List<SettlementPaymentResponse>> paymentsBySettlement = dtos.stream()
        .collect(Collectors.groupingBy(PaymentAllocationDto::paymentId))
        .values().stream()
        .map(group -> {
          PaymentAllocationDto first = group.get(0);
          List<String> allocationNames = group.stream()
              .map(PaymentAllocationDto::allocationUserId)
              .filter(Objects::nonNull)
              .map(uid -> userNameMap.getOrDefault(uid, "알 수 없는 사용자"))
              .distinct()
              .sorted()
              .toList();

          return new AbstractMap.SimpleEntry<>(
              first.settlementId(),
              new SettlementPaymentResponse(
                  first.title(),
                  first.paidAmount(),
                  userNameMap.get(first.payerId()),
                  allocationNames
              )
          );
        })
        .collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));

    // 8. Settlement -> SettlementHistoryResponse 변환
    List<SettlementHistoryResponse> responses = settlements.stream()
        .map(settlement -> new SettlementHistoryResponse(
            settlement.getId(),
            settlement.getTotalAmount(),
            settlement.getStatus(),
            settlement.getIssuedAt(),
            settlement.getSucceededAt(),
            settlementAmountMap.getOrDefault(settlement.getId(), 0L),
            paymentsBySettlement.getOrDefault(settlement.getId(), List.of())
        ))
        .toList();

    return new CommonPage<>(responses, settlementPage.getTotalPages());
  }

  /**
   * 비즈니스 규칙 검증
   */
  private void validatePaymentRequests(List<PaymentRequest> paymentRequests) {
    if (paymentRequests == null || paymentRequests.isEmpty()) {
      throw new BusinessException(SettlementErrorCode.EMPTY_PAYMENT_REQUESTS);
    }

    for (PaymentRequest paymentRequest : paymentRequests) {
      if (paymentRequest.paidAmount() <= 0) {
        throw new BusinessException(SettlementErrorCode.INVALID_PAYMENT_AMOUNT);
      }

      List<Long> allocationIds = paymentRequest.allocationIds();

      if (allocationIds == null || allocationIds.isEmpty()) {
        throw new BusinessException(SettlementErrorCode.EMPTY_ALLOCATION_TARGETS);
      }

      // 중복 검증
      Set<Long> uniqueIds = new HashSet<>(allocationIds);
      if (uniqueIds.size() != allocationIds.size()) {
        throw new BusinessException(SettlementErrorCode.DUPLICATE_ALLOCATION_TARGETS);
      }
    }
  }

  private record SettlementCalculation(long totalAmount, long totalRemainder,
                                       Map<Long, Long> netBalance) {
  }
}
