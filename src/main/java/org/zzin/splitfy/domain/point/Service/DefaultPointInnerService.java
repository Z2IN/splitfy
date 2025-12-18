package org.zzin.splitfy.domain.point.Service;

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
