package org.zzin.splitfy.domain.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;
import org.zzin.splitfy.domain.auth.service.DefaultAuthInnerService;

@ExtendWith(MockitoExtension.class)
public class AuthInnerServiceTest {

  @Mock
  private AuthRepository authRepository;

  @InjectMocks
  private DefaultAuthInnerService defaultAuthInnerService;

  @Test
  @DisplayName("충분한 잔액이 있을 때 포인트 송금에 성공한다")
  void 충분한_잔액이_있을_때_포인트_송금에_성공한다() {
    User sender = User.ofSignup("sender@test.com", "sender", "password");
    ReflectionTestUtils.setField(sender, "id", 1L);
    sender.addPoint(1000L);

    User receiver = User.ofSignup("receiver@test.com", "receiver", "password");
    ReflectionTestUtils.setField(receiver, "id", 2L);
    receiver.addPoint(500L);

    when(authRepository.findById(1L)).thenReturn(Optional.of(sender));
    when(authRepository.findById(2L)).thenReturn(Optional.of(receiver));

    PointTransferSummaryDTO result = defaultAuthInnerService.transferPoint(1L, 2L, 300L);

    assertNotNull(result);
    assertEquals(1000L, result.getSenderBeforePoint());
    assertEquals(700L, result.getSenderAfterPoint());
    assertEquals(500L, result.getReceiverBeforePoint());
    assertEquals(800L, result.getReceiverAfterPoint());
    assertEquals(700L, sender.getPoint());
    assertEquals(800L, receiver.getPoint());
  }

  @Test
  @DisplayName("송금액이 0일 때 예외가 발생한다")
  void 송금액이_0일_때_예외가_발생한다() {
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 2L, 0L);
    });

    assertEquals(AuthErrorCode.INVALID_POINT_BALANCE, exception.getErrorCode());
  }

  @Test
  @DisplayName("송금액이 음수일 때 예외가 발생한다")
  void 송금액이_음수일_때_예외가_발생한다() {
    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 2L, -100L);
    });

    assertEquals(AuthErrorCode.INVALID_POINT_BALANCE, exception.getErrorCode());
  }

  @Test
  @DisplayName("본인에게 송금할 때 예외가 발생한다")
  void 본인에게_송금할_때_예외가_발생한다() {
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 1L, 100L);
    });

    assertEquals(AuthErrorCode.CANNOT_TRANSFER_TO_SELF, exception.getErrorCode());
  }

  @Test
  @DisplayName("송신자가 존재하지 않을 때 예외가 발생한다")
  void 송신자가_존재하지_않을_때_예외가_발생한다() {
    when(authRepository.findById(1L)).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 2L, 100L);
    });

    assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("수신자가 존재하지 않을 때 예외가 발생한다")
  void 수신자가_존재하지_않을_때_예외가_발생한다() {
    User sender = User.ofSignup("sender@test.com", "sender", "password");
    ReflectionTestUtils.setField(sender, "id", 1L);
    sender.addPoint(1000L);

    when(authRepository.findById(1L)).thenReturn(Optional.of(sender));
    when(authRepository.findById(2L)).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 2L, 100L);
    });

    assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("송신자의 잔액이 부족할 때 예외가 발생한다")
  void 송신자의_잔액이_부족할_때_예외가_발생한다() {
    User sender = User.ofSignup("sender@test.com", "sender", "password");
    ReflectionTestUtils.setField(sender, "id", 1L);
    sender.addPoint(100L);

    User receiver = User.ofSignup("receiver@test.com", "receiver", "password");
    ReflectionTestUtils.setField(receiver, "id", 2L);

    when(authRepository.findById(1L)).thenReturn(Optional.of(sender));
    when(authRepository.findById(2L)).thenReturn(Optional.of(receiver));

    BusinessException exception = assertThrows(BusinessException.class, () -> {
      defaultAuthInnerService.transferPoint(1L, 2L, 500L);
    });

    assertEquals(AuthErrorCode.INSUFFICIENT_POINT_BALANCE, exception.getErrorCode());
  }
}
