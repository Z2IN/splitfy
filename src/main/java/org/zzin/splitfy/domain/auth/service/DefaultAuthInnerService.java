package org.zzin.splitfy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
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
}
