package org.zzin.splitfy.domain.point.Service;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.dto.response.TransferResponse;
import org.zzin.splitfy.domain.point.exception.PointErrorCode;
import org.zzin.splitfy.domain.point.mapper.PointMapper;
import org.zzin.splitfy.domain.point.model.UserPoint;
import org.zzin.splitfy.domain.point.repository.PointQRepository;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

import lombok.RequiredArgsConstructor;

@Service
@NullMarked
@RequiredArgsConstructor
public class PointService {

  private final AuthInnerService authInnerService;
  private final TransactionInnerService transactionInnerService;
  private final PointMapper pointMapper;
  private final PointQRepository pointQRepository;

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
    TransactionInfoDTO param = pointMapper.toTransactionInfoDTO(
        transactionUUID,
        userId,
        amount,
        pointChangeDetail
    );
    transactionInnerService.createDepositTransaction(param);

    return new DepositResponse(amount, pointChangeDetail.getAfterPoint());
  }

  /**
   * 지정된 사용자(toUserId)에게 현재 사용자인(meId)의 포인트를 전송합니다.
   *
   * @param toUserId 포인트를 받을 사용자의 ID
   * @param amount   전송할 포인트 금액(양수)
   * @param meId     포인트를 보내는(현재 인증된) 사용자의 ID
   * @return TransferResponse 전송된 금액과 송신자(meId)의 포인트 변경(이전/이후) 정보를 포함한 응답
   * @throws BusinessException
   *                           - 수신자(toUserId)가 존재하지 않거나(또는 송신자(meId)를 조회할 수 없을 경우) 발생합니다.
   * @throws BusinessException
   *                           - 송신자의 잔액이 전송할 금액보다 부족할 경우 발생합니다.
   * @implNote 트랜잭션 추적을 위해 UUID를 생성하여 트랜잭션 기록 생성 시 사용합니다.
   */
  @Transactional
  public TransferResponse transferTo(long toUserId, long amount, long meId) {
    String transactionUUID = UUID.randomUUID().toString();
    @Nullable UserPoint myPoint = pointQRepository.findUserPointBy(meId);

    if (myPoint == null) {
      throw new BusinessException(PointErrorCode.SENDER_NOT_FOUND);
    }

    if (!pointQRepository.isUserExistBy(toUserId)) {
      throw new BusinessException(PointErrorCode.RECEIVER_NOT_FOUND);
    }

    if (!myPoint.hasEnoughPoint(amount)) {
      throw new BusinessException(PointErrorCode.NOT_ENOUGH_POINT);
    }

    UserPointChangeDetailDTO myPointChangeDetail = authInnerService.addPoint(meId, amount * -1);
    UserPointChangeDetailDTO otherPointChangeDetail = authInnerService.addPoint(toUserId, amount);
    var transferOutInfo = pointMapper.toTransactionInfoDTO(
        transactionUUID,
        meId,
        amount,
        myPointChangeDetail
    );

    transactionInnerService.createTransferOutTransaction(transferOutInfo);

    var transferInInfo = pointMapper.toTransactionInfoDTO(
        transactionUUID,
        toUserId,
        amount,
        otherPointChangeDetail
    );

    transactionInnerService.createTransferInTransaction(transferInInfo);

    return TransferResponse.from(amount, myPointChangeDetail);

  }

}
