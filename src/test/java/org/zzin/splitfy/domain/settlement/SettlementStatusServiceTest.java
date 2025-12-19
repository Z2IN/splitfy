package org.zzin.splitfy.domain.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.domain.settlement.entity.Settlement;
import org.zzin.splitfy.domain.settlement.enums.SettlementStatus;
import org.zzin.splitfy.domain.settlement.repository.SettlementRepository;
import org.zzin.splitfy.domain.settlement.service.SettlementStatusService;

@ExtendWith(MockitoExtension.class)
class SettlementStatusServiceTest {

  @InjectMocks
  private SettlementStatusService settlementStatusService;

  @Mock
  private SettlementRepository settlementRepository;

  @Test
  void updateSettlementStatus_이체성공인경우_정산상태가SUCCEEDED로변경된다() {
    Long settlementId = 1L;

    Settlement settlement = new Settlement(10L, 10000L, 0L);
    ReflectionTestUtils.setField(settlement, "id", settlementId);
    ReflectionTestUtils.setField(settlement, "status", SettlementStatus.PENDING);

    given(settlementRepository.findById(settlementId))
        .willReturn(Optional.of(settlement));

    settlementStatusService.updateSettlementStatus(settlementId, true);

    assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.SUCCEEDED);
    assertThat(settlement.getSucceededAt()).isNotNull();

    then(settlementRepository).should(times(1))
        .findById(settlementId);
  }

  @Test
  void updateSettlementStatus_이체실패인경우_정산상태가FAILED로변경된다() {
    Long settlementId = 2L;

    Settlement settlement = new Settlement(10L, 20000L, 0L);
    ReflectionTestUtils.setField(settlement, "id", settlementId);
    ReflectionTestUtils.setField(settlement, "status", SettlementStatus.PENDING);

    given(settlementRepository.findById(settlementId))
        .willReturn(Optional.of(settlement));

    settlementStatusService.updateSettlementStatus(settlementId, false);

    assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);
    assertThat(settlement.getSucceededAt()).isNull();

    then(settlementRepository).should(times(1))
        .findById(settlementId);
  }
}