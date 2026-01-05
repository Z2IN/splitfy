package org.zzin.splitfy.domain.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.entity.Payment;
import org.zzin.splitfy.domain.settlement.entity.PaymentAllocations;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.entity.SettlementParticipant;
import org.zzin.splitfy.domain.settlement.repository.PaymentAllocationsRepository;
import org.zzin.splitfy.domain.settlement.repository.PaymentRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementParticipantRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;
import org.zzin.splitfy.domain.settlement.service.SettlementRecordService;

@ExtendWith(MockitoExtension.class)
class SettlementRecordServiceTest {

  @InjectMocks
  private SettlementRecordService settlementRecordService;

  @Mock
  private SettlementRepository settlementRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private SettlementParticipantRepository settlementParticipantRepository;

  @Mock
  private PaymentAllocationsRepository paymentAllocationsRepository;

  @Test
  void createSettlementRecord_정상요청시_정산과결제및참여자가저장된다() {
    long issuerId = 1L;
    long totalAmount = 20_000L;
    long totalRemainder = 0L;

    PaymentRequest paymentRequest1 = new PaymentRequest(
        "점심값",
        10_000L,
        1L,
        List.of(1L, 2L)
    );

    PaymentRequest paymentRequest2 = new PaymentRequest(
        "커피값",
        10_000L,
        2L,
        List.of(1L, 2L)
    );

    List<PaymentRequest> paymentRequests = List.of(paymentRequest1, paymentRequest2);

    Map<Long, Long> netBalance = Map.of(
        1L, 5_000L,
        2L, -5_000L
    );

    Settlement savedSettlement = new Settlement(issuerId, totalAmount, totalRemainder);
    ReflectionTestUtils.setField(savedSettlement, "id", 100L);

    given(settlementRepository.save(any(Settlement.class)))
        .willReturn(savedSettlement);

    given(paymentRepository.save(any(Payment.class)))
        .willAnswer(invocation -> {
          Payment payment = invocation.getArgument(0);
          ReflectionTestUtils.setField(payment, "id", 1L);
          return payment;
        });

    given(settlementParticipantRepository.saveAll(any(List.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    given(paymentAllocationsRepository.saveAll(any(List.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    Long settlementId = settlementRecordService.createSettlementRecord(
        issuerId,
        totalAmount,
        totalRemainder,
        paymentRequests,
        netBalance
    );

    assertThat(settlementId).isEqualTo(100L);

    then(settlementRepository).should(times(1))
        .save(any(Settlement.class));

    then(paymentRepository).should(times(2))
        .save(any(Payment.class));

    then(settlementParticipantRepository).should(times(1))
        .saveAll(any(List.class));

    then(paymentAllocationsRepository).should(times(1))
        .saveAll(any(List.class));
  }
}