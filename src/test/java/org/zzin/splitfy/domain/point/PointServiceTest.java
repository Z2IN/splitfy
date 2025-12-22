package org.zzin.splitfy.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.dto.response.TransferResponse;
import org.zzin.splitfy.domain.point.entity.UserPoint;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.repository.UserPointRepository;
import org.zzin.splitfy.domain.point.service.PointService;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

  @Mock
  private TransactionInnerService transactionInnerService;

  @Mock
  private UserPointRepository userPointRepository;

  @InjectMocks
  private PointService pointService;

  @Test
  void deposit_제대로된_데이터가_주어지면_입금_성공() {
    // 테스트 데이터
    long userId = 42L;
    long amount = 50L;
    long beforePoint = 100L;
    long afterPoint = 150L;

    UserPoint userPoint = new UserPoint(userId);
    userPoint.addPoint(beforePoint);

    given(userPointRepository.findByUserId(userId)).willReturn(Optional.of(userPoint));

    DepositResponse response = pointService.deposit(new AuthUser(userId), amount);

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualTo(amount);
    assertThat(response.point()).isEqualTo(afterPoint);

    then(userPointRepository).should().findByUserId(userId);

    ArgumentCaptor<TransactionInfoDTO> captor = ArgumentCaptor.forClass(TransactionInfoDTO.class);
    then(transactionInnerService).should().createDepositTransaction(captor.capture());

    TransactionInfoDTO dto = captor.getValue();
    assertThat(dto).isNotNull();
    assertThat(dto.getUserId()).isEqualTo(userId);
    assertThat(dto.getAmount()).isEqualTo(amount);
    assertThat(dto.getBeforePoint()).isEqualTo(beforePoint);
    assertThat(dto.getAfterPoint()).isEqualTo(afterPoint);
    assertThat(dto.getTransactionUUID()).isNotBlank();
  }

  @Test
  void transferTo_정상적으로_송금되면_성공() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 50L;

    long myBefore = 100L;
    long myAfter = 50L;
    long otherBefore = 0L;
    long otherAfter = 50L;

    UserPoint sender = new UserPoint(meId);
    sender.addPoint(myBefore);
    UserPoint receiver = new UserPoint(toUserId);

    given(userPointRepository.findByUserId(meId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(toUserId)).willReturn(Optional.of(receiver));

    TransferResponse response = pointService.transferTo(toUserId, amount, new AuthUser(meId));

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualTo(amount);
    assertThat(response.beforePoint()).isEqualTo(myBefore);
    assertThat(response.afterPoint()).isEqualTo(myAfter);

    then(userPointRepository).should().findByUserId(meId);
    then(userPointRepository).should().findByUserId(toUserId);

    assertThat(sender.getPoint()).isEqualTo(myAfter);
    assertThat(receiver.getPoint()).isEqualTo(otherAfter);

    ArgumentCaptor<TransactionInfoDTO> captor = ArgumentCaptor.forClass(TransactionInfoDTO.class);
    then(transactionInnerService).should().createTransferOutTransaction(captor.capture());
    TransactionInfoDTO out = captor.getValue();
    assertThat(out.getUserId()).isEqualTo(meId);
    assertThat(out.getAmount()).isEqualTo(amount);
    assertThat(out.getBeforePoint()).isEqualTo(myBefore);
    assertThat(out.getAfterPoint()).isEqualTo(myAfter);
    assertThat(out.getTransactionUUID()).isNotBlank();

    ArgumentCaptor<TransactionInfoDTO> captorIn = ArgumentCaptor.forClass(TransactionInfoDTO.class);
    then(transactionInnerService).should().createTransferInTransaction(captorIn.capture());
    TransactionInfoDTO in = captorIn.getValue();
    assertThat(in.getUserId()).isEqualTo(toUserId);
    assertThat(in.getAmount()).isEqualTo(amount);
    assertThat(in.getBeforePoint()).isEqualTo(otherBefore);
    assertThat(in.getAfterPoint()).isEqualTo(otherAfter);
    assertThat(in.getTransactionUUID()).isNotBlank();
    assertThat(out.getTransactionUUID()).isEqualTo(in.getTransactionUUID());
  }

  @Test
  void transferTo_금액이_0이하면_예외() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 0L;

    UserPoint sender = new UserPoint(meId);
    sender.addPoint(100L);
    UserPoint receiver = new UserPoint(toUserId);

    given(userPointRepository.findByUserId(meId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(toUserId)).willReturn(Optional.of(receiver));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> pointService.transferTo(toUserId, amount, new AuthUser(meId)));

    assertThat(ex.getErrorCode()).isEqualTo(
        PointErrorCode.INVALID_POINT_AMOUNT);

    then(userPointRepository).should().findByUserId(meId);
    then(userPointRepository).should().findByUserId(toUserId);
    then(transactionInnerService).shouldHaveNoInteractions();
  }

  @Test
  void transferTo_자기자신에게_송금하면_예외() {
    long meId = 1L;
    long toUserId = meId;
    long amount = 10L;

    BusinessException ex = assertThrows(BusinessException.class,
        () -> pointService.transferTo(toUserId, amount, new AuthUser(meId)));

    assertThat(ex.getErrorCode()).isEqualTo(
        PointErrorCode.CANNOT_TRANSFER_TO_SELF);

    then(userPointRepository).shouldHaveNoInteractions();
    then(transactionInnerService).shouldHaveNoInteractions();
  }

  @Test
  void transferTo_송신자_없으면_예외() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 10L;

    given(userPointRepository.findByUserId(meId)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class,
        () -> pointService.transferTo(toUserId, amount, new AuthUser(meId)));

    assertThat(ex.getErrorCode()).isEqualTo(
        PointErrorCode.USER_NOT_FOUND);

    then(userPointRepository).should().findByUserId(meId);
    then(transactionInnerService).shouldHaveNoInteractions();
  }

  @Test
  void transferTo_수신자_없으면_예외() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 10L;

    UserPoint sender = new UserPoint(meId);
    sender.addPoint(100L);

    given(userPointRepository.findByUserId(meId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(toUserId)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class,
        () -> pointService.transferTo(toUserId, amount, new AuthUser(meId)));

    assertThat(ex.getErrorCode()).isEqualTo(
        PointErrorCode.USER_NOT_FOUND);

    then(userPointRepository).should().findByUserId(meId);
    then(userPointRepository).should().findByUserId(toUserId);
    then(transactionInnerService).shouldHaveNoInteractions();
  }

  @Test
  void transferTo_잔액부족이면_예외() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 100L;

    UserPoint sender = new UserPoint(meId);
    UserPoint receiver = new UserPoint(toUserId);

    given(userPointRepository.findByUserId(meId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(toUserId)).willReturn(Optional.of(receiver));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> pointService.transferTo(toUserId, amount, new AuthUser(meId)));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE);

    then(userPointRepository).should().findByUserId(meId);
    then(userPointRepository).should().findByUserId(toUserId);
    then(transactionInnerService).shouldHaveNoInteractions();
  }
}
