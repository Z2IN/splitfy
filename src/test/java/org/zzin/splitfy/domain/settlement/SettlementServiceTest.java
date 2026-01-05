package org.zzin.splitfy.domain.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.service.SettlementRecordService;
import org.zzin.splitfy.domain.settlement.service.SettlementService;
import org.zzin.splitfy.domain.settlement.service.SettlementStatusService;
import org.zzin.splitfy.domain.settlement.service.SettlementTransferService;
import org.zzin.splitfy.domain.settlement.exception.SettlementErrorCode;

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
        .executeTransfers(anyMap(), anyLong());

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response.id()).isEqualTo(100L);

    then(settlementRecordService).should(times(1))
        .createSettlementRecord(anyLong(), anyLong(), anyLong(), anyList(), anyMap());

    then(settlementTransferService).should(times(1))
        .executeTransfers(anyMap(), anyLong());

    then(settlementStatusService).shouldHaveNoInteractions();
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
        .executeTransfers(anyMap(), anyLong());

    willDoNothing()
        .given(settlementStatusService)
        .updateSettlementStatus(anyLong(), eq(false));

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response.id()).isEqualTo(200L);

    then(settlementRecordService).should(times(1))
        .createSettlementRecord(anyLong(), anyLong(), anyLong(), anyList(), anyMap());

    then(settlementTransferService).should(times(1))
        .executeTransfers(anyMap(), anyLong());

    then(settlementStatusService).should(times(1))
        .updateSettlementStatus(200L, false);
  }

  @Test
  void createSettlement_비어있는결제요청일때_예외를낸다() {
    AuthUser authUser = new AuthUser(1L);
    SettlementRequest request = new SettlementRequest(List.of());

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
            .isEqualTo(SettlementErrorCode.EMPTY_PAYMENT_REQUESTS));
  }

  @Test
  void createSettlement_정산대상자가없으면_예외를낸다() {
    AuthUser authUser = new AuthUser(1L);
    PaymentRequest paymentRequest = new PaymentRequest("회식비", 10_000L, 1L, List.of());
    SettlementRequest request = new SettlementRequest(List.of(paymentRequest));

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
            .isEqualTo(SettlementErrorCode.EMPTY_ALLOCATION_TARGETS));
  }

  @Test
  void createSettlement_결제금액이0이하면_예외를낸다() {
    AuthUser authUser = new AuthUser(1L);
    PaymentRequest paymentRequest = new PaymentRequest("회식비", 0L, 1L, List.of(1L));
    SettlementRequest request = new SettlementRequest(List.of(paymentRequest));

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
            .isEqualTo(SettlementErrorCode.INVALID_PAYMENT_AMOUNT));
  }

  @Test
  void createSettlement_중복정산대상자가있으면_예외를낸다() {
    AuthUser authUser = new AuthUser(1L);
    PaymentRequest paymentRequest = new PaymentRequest("회식비", 10_000L, 1L, List.of(1L, 1L));
    SettlementRequest request = new SettlementRequest(List.of(paymentRequest));

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
            .isEqualTo(SettlementErrorCode.DUPLICATE_ALLOCATION_TARGETS));
  }
}