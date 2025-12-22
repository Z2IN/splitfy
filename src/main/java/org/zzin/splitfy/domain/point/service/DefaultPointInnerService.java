package org.zzin.splitfy.domain.point.service;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.point.entity.UserPoint;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.repository.UserPointRepository;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

import lombok.RequiredArgsConstructor;

@NullMarked
@Service
@RequiredArgsConstructor
public class DefaultPointInnerService implements PointInnerService {

  private final UserPointRepository userPointRepository;
  private final TransactionInnerService transactionInnerService;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public long initUserPoint(long userId) {
    UserPoint userPoint = new UserPoint(userId);
    return userPointRepository.save(userPoint).getPoint();
  }

  /**
   * 송신자로부터 수신자에게 포인트를 이체합니다. (자동 이체용)
   *
   * @param fromUserId 송신자의 사용자 ID
   * @param toUserId   수신자의 사용자 ID
   * @param amount     이체할 포인트 양 (양수여야 함)
   */
  @Override
  @Transactional
  public void transferPoint(long fromUserId, long toUserId, long amount) {
    // TODO: PointService.transferTo 로직을 참고하여 구현
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void sendEventRewardPoint(long userId, long reward) {
    UserPoint userPoint = userPointRepository.findByUserId(userId)
        .orElseThrow(() -> new BusinessException(PointErrorCode.USER_NOT_FOUND));
    long beforePoint = userPoint.getPoint();
    userPoint.addPoint(reward);
    long afterPoint = userPoint.getPoint();

    transactionInnerService.createRewardTransaction(
        TransactionInfoDTO.builder()
            .transactionUUID(UUID.randomUUID().toString())
            .userId(userId)
            .amount(reward)
            .beforePoint(beforePoint)
            .afterPoint(afterPoint)
            .build()
    );

    userPointRepository.save(userPoint);
  }
}
