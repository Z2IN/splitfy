package org.zzin.splitfy.domain.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
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
import org.zzin.splitfy.domain.settlement.service.SettlementService;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

  @Mock
  private SettlementRepository settlementRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private SettlementParticipantRepository settlementParticipantRepository;

  @InjectMocks
  private SettlementService settlementService;

  @Test
  void createSettlement_정상적인_정산요청이_주어지면_정산생성_성공() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    PaymentRequest payment = new PaymentRequest(
        "점심 식사",
        30000L,
        1L,
        List.of(1L, 2L, 3L)
    );
    SettlementRequest request = new SettlementRequest(List.of(payment));

    Settlement savedSettlement = new Settlement(issuerId, 30000L);
    ReflectionTestUtils.setField(savedSettlement, "id", 1L);
    given(settlementRepository.save(any(Settlement.class))).willReturn(savedSettlement);

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(savedSettlement.getId());

    then(settlementRepository).should(times(1)).save(any(Settlement.class));
    then(paymentRepository).should(times(1)).save(any(Payment.class));
    then(settlementParticipantRepository).should(times(3)).save(any(SettlementParticipant.class));
  }

  @Test
  void createSettlement_중복된_정산대상자가_있으면_예외발생() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    PaymentRequest payment = new PaymentRequest(
        "점심 식사",
        30000L,
        1L,
        List.of(1L, 2L, 2L)
    );
    SettlementRequest request = new SettlementRequest(List.of(payment));

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(SettlementErrorCode.DUPLICATE_ALLOCATION_TARGETS.getMessage());

    then(settlementRepository).should(times(0)).save(any(Settlement.class));
  }

  @Test
  void createSettlement_결제자가_정산대상자에_포함되지않으면_예외발생() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    PaymentRequest payment = new PaymentRequest(
        "점심 식사",
        30000L,
        1L,
        List.of(2L, 3L)
    );
    SettlementRequest request = new SettlementRequest(List.of(payment));

    assertThatThrownBy(() -> settlementService.createSettlement(authUser, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(SettlementErrorCode.SELF_ALLOCATION_NOT_ALLOWED.getMessage());

    then(settlementRepository).should(times(0)).save(any(Settlement.class));
  }

  @Test
  void createSettlement_복잡한_정산_계산이_정확하게_수행됨() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    // 사용자 1이 30000원 결제, 1,2,3이 분담 (각 10000원)
    PaymentRequest payment1 = new PaymentRequest(
        "점심 식사",
        30000L,
        1L,
        List.of(1L, 2L, 3L)
    );

    // 사용자 2가 20000원 결제, 2,3이 분담 (각 10000원)
    PaymentRequest payment2 = new PaymentRequest(
        "커피",
        20000L,
        2L,
        List.of(2L, 3L)
    );

    SettlementRequest request = new SettlementRequest(List.of(payment1, payment2));

    Settlement savedSettlement = new Settlement(issuerId, 50000L);
    ReflectionTestUtils.setField(savedSettlement, "id", 1L);
    given(settlementRepository.save(any(Settlement.class))).willReturn(savedSettlement);

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    // then
    assertThat(response).isNotNull();

    // Settlement 저장 검증
    ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
    then(settlementRepository).should(times(1)).save(settlementCaptor.capture());
    Settlement capturedSettlement = settlementCaptor.getValue();
    assertThat(capturedSettlement.getIssuerId()).isEqualTo(issuerId);
    assertThat(capturedSettlement.getTotalAmount()).isEqualTo(
        50000L); // 10000(사용자1) + 20000(사용자2) + 20000(사용자3)

    // Payment 저장 검증 (2건)
    then(paymentRepository).should(times(2)).save(any(Payment.class));

    // SettlementParticipant 저장 검증 (3명)
    // 사용자 1: 30000 기여 - 10000 부담 = +20000 (받을 금액)
    // 사용자 2: 20000 기여 - 20000 부담 = 0 (없음)
    // 사용자 3: 0 기여 - 20000 부담 = -20000 (줘야 할 금액)
    ArgumentCaptor<SettlementParticipant> participantCaptor = ArgumentCaptor.forClass(
        SettlementParticipant.class);
    then(settlementParticipantRepository).should(times(2)).save(participantCaptor.capture());
  }

  @Test
  void createSettlement_나머지_금액은_정산에_포함되지않음() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    // 10000원을 3명이 분담하면 각 3333원, 나머지 1원은 splitfy 부담
    PaymentRequest payment = new PaymentRequest(
        "회식",
        10000L,
        1L,
        List.of(1L, 2L, 3L)
    );
    SettlementRequest request = new SettlementRequest(List.of(payment));

    Settlement savedSettlement = new Settlement(issuerId, 9999L);
    ReflectionTestUtils.setField(savedSettlement, "id", 1L);
    given(settlementRepository.save(any(Settlement.class))).willReturn(savedSettlement);

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
    then(settlementRepository).should(times(1)).save(settlementCaptor.capture());
    Settlement capturedSettlement = settlementCaptor.getValue();

    assertThat(capturedSettlement.getTotalAmount()).isEqualTo(9999L);
  }

  @Test
  void createSettlement_여러_결제건의_정산금액이_정확하게_합산됨() {
    long issuerId = 1L;
    AuthUser authUser = new AuthUser(issuerId);

    PaymentRequest payment1 = new PaymentRequest(
        "간식",
        15000L,
        1L,
        List.of(1L, 2L, 3L)
    );

    PaymentRequest payment2 = new PaymentRequest(
        "음료",
        18000L,
        3L,
        List.of(1L, 2L, 3L)
    );

    SettlementRequest request = new SettlementRequest(List.of(payment1, payment2));

    Settlement savedSettlement = new Settlement(issuerId, 33000L);
    ReflectionTestUtils.setField(savedSettlement, "id", 1L);
    given(settlementRepository.save(any(Settlement.class))).willReturn(savedSettlement);

    SettlementResponse response = settlementService.createSettlement(authUser, request);

    assertThat(response).isNotNull();

    ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
    then(settlementRepository).should(times(1)).save(settlementCaptor.capture());
    Settlement capturedSettlement = settlementCaptor.getValue();

    // 총 정산 금액: 각 사용자당 11000원 (5000 + 6000) * 3명 = 33000원
    assertThat(capturedSettlement.getTotalAmount()).isEqualTo(33000L);

    // 사용자 1: 15000 기여 - 11000 부담 = +4000 (받을 금액)
    // 사용자 2: 0 기여 - 11000 부담 = -11000 (줘야 할 금액)
    // 사용자 3: 18000 기여 - 11000 부담 = +7000 (받을 금액)
    then(settlementParticipantRepository).should(times(3)).save(any(SettlementParticipant.class));
  }
}