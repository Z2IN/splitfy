package org.zzin.splitfy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;

@Service
@RequiredArgsConstructor
public class DefaultAuthInnerService implements AuthInnerService {

  private final AuthRepository authRepository;

  @Override
  @Transactional(readOnly = true)
  public long getPointBy(long userId) {
    User user = authRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

    return user.getPoint();
  }

  @Override
  @Transactional
  public PointChangeResultDTO addPoint(long userId, long amount) {
    User user = authRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

    long previousPoint = user.getPoint();
    user.addPoint(amount);

    return new PointChangeResultDTO(previousPoint, user.getPoint());
  }

  @Override
  @Transactional
  public PointTransferSummaryDTO transferPoint(long senderId, long receiverId, long amount) {
    // 송금액 검증
    if (amount <= 0) {
      throw new BusinessException(AuthErrorCode.INVALID_POINT_BALANCE);
    }

    // 본인에게 송금 불가
    if (senderId == receiverId) {
      throw new BusinessException(AuthErrorCode.CANNOT_TRANSFER_TO_SELF);
    }

    // 송신자와 수신자 조회
    User sender = authRepository.findById(senderId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
    User receiver = authRepository.findById(receiverId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

    // 이전 포인트 저장
    long senderBeforePoint = sender.getPoint();
    long receiverBeforePoint = receiver.getPoint();

    sender.deductPoint(amount);
    receiver.addPoint(amount);

    return new PointTransferSummaryDTO(
        senderBeforePoint,
        sender.getPoint(),
        receiverBeforePoint,
        receiver.getPoint()
    );
  }


}
