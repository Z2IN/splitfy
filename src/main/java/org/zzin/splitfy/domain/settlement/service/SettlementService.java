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
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;
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

    // 최소 정산 알고리즘: 사용자별 정산 금액 계산
    Map<Long, Long> netBalance = calculateNetBalance(contributed, allocated);

    // 1. 정산 요청에 대한 settlement, payment 요청 기록 (첫 번째 트랜잭션)
    Long settlementId = settlementRecordService.createSettlementRecord(issuerId, totalAmount,
        totalRemainder, paymentRequests, netBalance);

    // 2. 이체 실행 (두 번째 트랜잭션)
    try {
      settlementTransferService.executeTransfers(netBalance);
      // 3. 이체 성공 시 settlement state 변경 (세 번째 트랜잭션)
      settlementStatusService.updateSettlementStatus(settlementId, true);
    } catch (Exception e) {
      // 3. 이체 실패 시 settlement state 변경 (세 번째 트랜잭션)
      settlementStatusService.updateSettlementStatus(settlementId, false);
    }

    return new SettlementResponse(settlementId);
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

    return netBalance;
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

    // 3. Repository를 통해 Payment와 PaymentAllocations를 한 번의 쿼리로 조회
    List<PaymentAllocationDto> dtos = settlementQueryRepository
        .findPaymentAllocationsInSettlements(settlementIds);

    // 4. User ID 수집
    Set<Long> userIds = new HashSet<>();
    for (PaymentAllocationDto dto : dtos) {
      userIds.add(dto.payerId());
      if (dto.allocationUserId() != null) {
        userIds.add(dto.allocationUserId());
      }
    }

    // 5. User 정보 조회
    Map<Long, String> userNameMap = authInnerService.findByIdIn(userIds);

    // 6. paymentId로 그룹화하여 SettlementPaymentResponse 생성 후 settlementId로 재그룹화
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

    // 7. Settlement -> SettlementHistoryResponse 변환
    List<SettlementHistoryResponse> responses = settlements.stream()
        .map(settlement -> new SettlementHistoryResponse(
            settlement.getId(),
            settlement.getTotalAmount(),
            settlement.getStatus(),
            settlement.getIssuedAt(),
            settlement.getSucceededAt(),
            settlement.getRemainder(),
            paymentsBySettlement.getOrDefault(settlement.getId(), List.of())
        ))
        .toList();

    return new CommonPage<>(responses, settlementPage.getTotalPages());
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
}
