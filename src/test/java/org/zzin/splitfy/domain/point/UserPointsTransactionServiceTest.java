package org.zzin.splitfy.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.point.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.point.entity.UserPoint;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.repository.UserPointRepository;
import org.zzin.splitfy.domain.point.service.UserPointsTransactionService;

@ExtendWith(MockitoExtension.class)
class UserPointsTransactionServiceTest {

  @Mock
  private UserPointRepository userPointRepository;

  @InjectMocks
  private UserPointsTransactionService userPointsTransactionService;

  @Test
  void transferPoints_정상적으로_송금되면_성공() {
    long senderId = 1L;
    long receiverId = 2L;
    long amount = 50L;

    long senderBefore = 100L;
    long senderAfter = 50L;
    long receiverBefore = 0L;
    long receiverAfter = 50L;

    UserPoint sender = new UserPoint(senderId);
    sender.addPoint(senderBefore);
    UserPoint receiver = new UserPoint(receiverId);

    given(userPointRepository.findByUserId(senderId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(receiverId)).willReturn(Optional.of(receiver));

    PointTransferSummaryDTO result = userPointsTransactionService.transferPoints(senderId,
        receiverId, amount);

    assertThat(result).isNotNull();
    assertThat(result.getSenderBeforePoint()).isEqualTo(senderBefore);
    assertThat(result.getSenderAfterPoint()).isEqualTo(senderAfter);
    assertThat(result.getReceiverBeforePoint()).isEqualTo(receiverBefore);
    assertThat(result.getReceiverAfterPoint()).isEqualTo(receiverAfter);

    then(userPointRepository).should().findByUserId(senderId);
    then(userPointRepository).should().findByUserId(receiverId);

    assertThat(sender.getPoint()).isEqualTo(senderAfter);
    assertThat(receiver.getPoint()).isEqualTo(receiverAfter);
  }

  @Test
  void transferPoints_금액이_0이하면_예외() {
    long senderId = 1L;
    long receiverId = 2L;
    long amount = 0L;

    UserPoint sender = new UserPoint(senderId);
    sender.addPoint(100L);
    UserPoint receiver = new UserPoint(receiverId);

    given(userPointRepository.findByUserId(senderId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(receiverId)).willReturn(Optional.of(receiver));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> userPointsTransactionService.transferPoints(senderId, receiverId, amount));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.INVALID_POINT_AMOUNT);
  }

  @Test
  void transferPoints_자기자신에게_송금하면_예외() {
    long senderId = 1L;
    long receiverId = senderId;
    long amount = 10L;

    BusinessException ex = assertThrows(BusinessException.class,
        () -> userPointsTransactionService.transferPoints(senderId, receiverId, amount));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.CANNOT_TRANSFER_TO_SELF);

    then(userPointRepository).shouldHaveNoInteractions();
  }

  @Test
  void transferPoints_송신자_없으면_예외() {
    long senderId = 1L;
    long receiverId = 2L;
    long amount = 10L;

    given(userPointRepository.findByUserId(senderId)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class,
        () -> userPointsTransactionService.transferPoints(senderId, receiverId, amount));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.USER_NOT_FOUND);

    then(userPointRepository).should().findByUserId(senderId);
  }

  @Test
  void transferPoints_수신자_없으면_예외() {
    long senderId = 1L;
    long receiverId = 2L;
    long amount = 10L;

    UserPoint sender = new UserPoint(senderId);
    sender.addPoint(100L);

    given(userPointRepository.findByUserId(senderId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(receiverId)).willReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class,
        () -> userPointsTransactionService.transferPoints(senderId, receiverId, amount));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.USER_NOT_FOUND);

    then(userPointRepository).should().findByUserId(senderId);
    then(userPointRepository).should().findByUserId(receiverId);
  }

  @Test
  void transferPoints_잔액부족이면_예외() {
    long senderId = 1L;
    long receiverId = 2L;
    long amount = 100L;

    UserPoint sender = new UserPoint(senderId);
    UserPoint receiver = new UserPoint(receiverId);

    given(userPointRepository.findByUserId(senderId)).willReturn(Optional.of(sender));
    given(userPointRepository.findByUserId(receiverId)).willReturn(Optional.of(receiver));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> userPointsTransactionService.transferPoints(senderId, receiverId, amount));

    assertThat(ex.getErrorCode()).isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE);

    then(userPointRepository).should().findByUserId(senderId);
    then(userPointRepository).should().findByUserId(receiverId);
  }
}
