package org.zzin.splitfy.domain.settlement;


import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.domain.point.Service.PointInnerService;
import org.zzin.splitfy.domain.settlement.repository.PaymentRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementParticipantRepository;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;
import org.zzin.splitfy.domain.settlement.service.SettlementTransferService;

@ExtendWith(MockitoExtension.class)
class SettlementTransferServiceTest {

  @InjectMocks
  private SettlementTransferService settlementTransferService;

  @Mock
  private PointInnerService pointInnerService;

  @Mock
  private SettlementRepository settlementRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private SettlementParticipantRepository settlementParticipantRepository;

  @Test
  void executeTransfers_채무자와채권자가있을때_정상적으로이체가수행된다() {
    // given
    Map<Long, Long> netBalance = Map.of(
        1L, 10_000L,   // 받을 사람
        2L, -6_000L,   // 줄 사람
        3L, -4_000L    // 줄 사람
    );

    settlementTransferService.executeTransfers(netBalance);

    then(pointInnerService).should(times(1))
        .transferPoint(2L, 1L, 6_000L);

    then(pointInnerService).should(times(1))
        .transferPoint(3L, 1L, 4_000L);

    then(pointInnerService).shouldHaveNoMoreInteractions();
  }

  @Test
  void executeTransfers_정산금액이0인사용자는_이체대상에서제외된다() {
    Map<Long, Long> netBalance = Map.of(
        1L, 5_000L,
        2L, -5_000L,
        3L, 0L          // 이체 대상 아님
    );

    settlementTransferService.executeTransfers(netBalance);

    then(pointInnerService).should(times(1))
        .transferPoint(2L, 1L, 5_000L);

    then(pointInnerService).shouldHaveNoMoreInteractions();
  }
}