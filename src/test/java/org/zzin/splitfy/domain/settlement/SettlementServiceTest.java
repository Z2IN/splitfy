package org.zzin.splitfy.domain.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyMap;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.settlement.dto.request.PaymentRequest;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.service.SettlementRecordService;
import org.zzin.splitfy.domain.settlement.service.SettlementService;
import org.zzin.splitfy.domain.settlement.service.SettlementStatusService;
import org.zzin.splitfy.domain.settlement.service.SettlementTransferService;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

  @InjectMocks
  private SettlementService settlementService;

  @Mock
  private SettlementRecordService settlementRecordService;

  @Mock
  private SettlementTransferService settlementTransferService;

  @Mock
  private SettlementStatusService settlementStatusService;

  @Test
  void createSettlement_이체성공시_정산상태가성공으로변경된다() {
    AuthUser authUser = new AuthUser(1L);

    PaymentRequest paymentRequest = new PaymentRequest(
        "점심값",
        10_000L,
        1L,
        List.of(1L, 2L)
    );

    SettlementRequest request = new SettlementRequest(List.of(paymentRequest));

    given(settlementRecordService.createSettlementRecord(
        anyLong(),
        anyLong(),
        anyLong(),
        anyList(),
        anyMap()
    )).willReturn(100L);

    willDoNothing()
        .given(settlementTransferService)
        .executeTransfers(anyMap());

    willDoNothing()
        .given(settlementStatusService)
        .updateSettlementStatus(anyLong(), eq(true));

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response.id()).isEqualTo(100L);

    then(settlementRecordService).should(times(1))
        .createSettlementRecord(anyLong(), anyLong(), anyLong(), anyList(), anyMap());

    then(settlementTransferService).should(times(1))
        .executeTransfers(anyMap());

    then(settlementStatusService).should(times(1))
        .updateSettlementStatus(100L, true);
  }

  @Test
  void createSettlement_이체실패시_정산상태가실패로변경된다() {
    AuthUser authUser = new AuthUser(1L);

    PaymentRequest paymentRequest = new PaymentRequest(
        "회식비",
        20_000L,
        1L,
        List.of(1L, 2L, 3L)
    );

    SettlementRequest request = new SettlementRequest(List.of(paymentRequest));

    given(settlementRecordService.createSettlementRecord(
        anyLong(),
        anyLong(),
        anyLong(),
        anyList(),
        anyMap()
    )).willReturn(200L);

    willThrow(new RuntimeException("이체 실패"))
        .given(settlementTransferService)
        .executeTransfers(anyMap());

    willDoNothing()
        .given(settlementStatusService)
        .updateSettlementStatus(anyLong(), eq(false));

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response.id()).isEqualTo(200L);

    then(settlementRecordService).should(times(1))
        .createSettlementRecord(anyLong(), anyLong(), anyLong(), anyList(), anyMap());

    then(settlementTransferService).should(times(1))
        .executeTransfers(anyMap());

    then(settlementStatusService).should(times(1))
        .updateSettlementStatus(200L, false);
  }
}