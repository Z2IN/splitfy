package org.zzin.splitfy.domain.settlement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.entity.Payment;
import org.zzin.splitfy.domain.settlement.entity.PaymentAllocations;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.entity.SettlementParticipant;
import org.zzin.splitfy.domain.settlement.repository.PaymentAllocationsRepository;
import org.zzin.splitfy.domain.settlement.repository.PaymentRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementParticipantRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class SettlementRecordService {

  private final SettlementRepository settlementRepository;
  private final PaymentRepository paymentRepository;
  private final SettlementParticipantRepository settlementParticipantRepository;
  private final PaymentAllocationsRepository paymentAllocationsRepository;

  /**
   * 정산 요청에 대한 settlement, payment, participant 기록을 저장합니다.
   *
   * @param issuerId        정산 발행자 ID
   * @param totalAmount     총 정산 금액
   * @param totalRemainder  총 나머지 금액
   * @param paymentRequests 결제 요청 목록
   * @param netBalance      사용자별 정산 금액
   * @return 생성된 Settlement ID
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long createSettlementRecord(
      long issuerId,
      long totalAmount,
      long totalRemainder,
      List<PaymentRequest> paymentRequests,
      Map<Long, Long> netBalance
  ) {
    // Settlement 생성 및 저장
    Settlement savedSettlement = settlementRepository.save(
        new Settlement(issuerId, totalAmount, totalRemainder)
    );

    // Payment 엔티티 생성 및 저장 + PaymentAllocations 저장
    List<PaymentAllocations> allAllocations = new ArrayList<>();
    for (PaymentRequest paymentRequest : paymentRequests) {
      Payment payment = savedSettlement.createPayment(
          paymentRequest.paidAmount(),
          paymentRequest.payerId(),
          paymentRequest.title()
      );
      payment = paymentRepository.save(payment);

      // PaymentAllocations 수집 (각 payment의 정산 대상자)
      for (Long allocationId : paymentRequest.allocationIds()) {
        allAllocations.add(new PaymentAllocations(payment.getId(), allocationId));
      }
    }
    paymentAllocationsRepository.saveAll(allAllocations);

    // SettlementParticipant 저장 (각 사용자의 정산 금액)
    List<SettlementParticipant> participants = netBalance.entrySet().stream()
        .map(entry -> savedSettlement.createParticipant(entry.getKey(), entry.getValue()))
        .toList();
    settlementParticipantRepository.saveAll(participants);

    return savedSettlement.getId();
  }

}
