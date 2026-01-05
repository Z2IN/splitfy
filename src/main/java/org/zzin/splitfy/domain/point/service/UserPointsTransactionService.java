package org.zzin.splitfy.domain.point.service;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.point.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.point.entity.UserPoint;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.repository.UserPointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserPointsTransactionService {

  private final UserPointRepository userPointRepository;

  /**
   * 특정 사용자(senderId)로부터 다른 사용자(receiverId)에게 지정한 양(amount)의 포인트를 이체한다.
   *
   * @param senderId   이체를 시작하는 사용자 ID
   * @param receiverId 이체를 받는 사용자 ID
   * @param amount     이체할 포인트 양
   * @return 이체 전후의 송신자 및 수신자 포인트 정보를 담은 {@link PointTransferSummaryDTO}
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public PointTransferSummaryDTO transferPoints(long senderId, long receiverId, long amount) {
    // 본인에게 송금 불가
    if (senderId == receiverId) {
      throw new BusinessException(PointErrorCode.CANNOT_TRANSFER_TO_SELF);
    }

    // 송신자와 수신자 조회
    UserPoint senderPoint = userPointRepository.findByUserId(senderId)
        .orElseThrow(() -> new BusinessException(PointErrorCode.USER_NOT_FOUND));
    UserPoint receiverPoint = userPointRepository.findByUserId(receiverId)
        .orElseThrow(() -> new BusinessException(PointErrorCode.USER_NOT_FOUND));

    // 이전 포인트 저장
    long senderBeforePoint = senderPoint.getPoint();
    long receiverBeforePoint = receiverPoint.getPoint();

    senderPoint.deductPoint(amount);
    receiverPoint.addPoint(amount);

    return new PointTransferSummaryDTO(
        senderBeforePoint,
        senderPoint.getPoint(),
        receiverBeforePoint,
        receiverPoint.getPoint()
    );
  }
}
