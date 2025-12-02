package org.zzin.splitfy.domain.point.Service;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.mapper.PointMapper;
import org.zzin.splitfy.domain.transaction.dto.CreateDepositTransactionParamDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

import lombok.RequiredArgsConstructor;

@Service
@NullMarked
@RequiredArgsConstructor
public class PointService {

  private final AuthInnerService authInnerService;
  private final TransactionInnerService transactionInnerService;
  private final PointMapper pointMapper;

  /**
   * 주어진 사용자 ID에 대한 포인트 잔액을 조회하여 반환합니다.
   *
   * @param userId 조회할 사용자의 고유 식별자
   * @return 요청한 사용자의 현재 포인트 잔액
   */
  public long getPointBy(long userId) {
    return authInnerService.getPointBy(userId);
  }

  /**
   * 지정된 사용자 계정에 포인트를 적립합니다.
   *
   * @param userId 포인트를 적립할 사용자 ID
   * @param amount 적립할 포인트 금액
   * @return DepositResponse 적립한 금액과 적립 후 사용자 포인트 잔액을 포함한 응답 객체
   * @implNote 트랜잭션 추적을 위해 UUID를 생성하여 트랜잭션 기록 생성 시 사용합니다.
   */
  @Transactional
  public DepositResponse deposit(long userId, long amount) {
    String transactionUUID = UUID.randomUUID().toString();
    UserPointChangeDetailDTO pointChangeDetail = authInnerService.addPoint(userId, amount);
    CreateDepositTransactionParamDTO param = pointMapper.toCreateDepositTransactionParamDTO(
        transactionUUID,
        userId,
        amount,
        pointChangeDetail
    );
    transactionInnerService.createDepositTransaction(param);

    return new DepositResponse(amount, pointChangeDetail.getAfterPoint());
  }

}
