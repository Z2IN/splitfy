package org.zzin.splitfy.domain.point.Service;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.dto.response.TransferResponse;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

import lombok.RequiredArgsConstructor;

@Service
@NullMarked
@RequiredArgsConstructor
public class PointService {

  private final AuthInnerService authInnerService;
  private final TransactionInnerService transactionInnerService;

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
    PointChangeResultDTO pointChangeDetail = authInnerService.addPoint(userId, amount);
    TransactionInfoDTO param = TransactionInfoDTO.builder()
        .transactionUUID(transactionUUID)
        .userId(userId)
        .amount(amount)
        .beforePoint(pointChangeDetail.getBeforePoint())
        .afterPoint(pointChangeDetail.getAfterPoint())
        .build();
    transactionInnerService.createDepositTransaction(param);

    return new DepositResponse(amount, pointChangeDetail.getAfterPoint());
  }

  /**
   * 송신자(meId)로부터 수신자(toUserId)에게 amount만큼 포인트를 이체하고, 이체 관련 트랜잭션을 기록합니다.
   *
   * 메서드는 @Transactional로 실행되어 모든 단계가 하나의 트랜잭션 경계 안에서 수행됩니다.
   * 내부 서비스 호출 중 예외가 발생하면 전체 작업은 롤백됩니다.
   *
   * @param toUserId 수신자의 사용자 ID
   * @param amount   이체할 포인트 양 (양수여야 함)
   * @param meId     송신자의 사용자 ID
   * @return TransferResponse — 요청한 이체 금액 및 송신자의 before/after 포인트 정보를 포함한 응답
   */
  @Transactional
  public TransferResponse transferTo(long toUserId, long amount, long meId) {
    String transactionUUID = UUID.randomUUID().toString();
    PointTransferSummaryDTO pointChangeDetail = authInnerService.transferPoint(meId, toUserId,
        amount);

    var transferOutInfo = TransactionInfoDTO.builder()
        .transactionUUID(transactionUUID)
        .userId(meId)
        .amount(amount)
        .beforePoint(pointChangeDetail.getSenderBeforePoint())
        .afterPoint(pointChangeDetail.getSenderAfterPoint())
        .build();

    transactionInnerService.createTransferOutTransaction(transferOutInfo);

    var transferInInfo = TransactionInfoDTO.builder()
        .transactionUUID(transactionUUID)
        .userId(toUserId)
        .amount(amount)
        .beforePoint(pointChangeDetail.getReceiverBeforePoint())
        .afterPoint(pointChangeDetail.getReceiverAfterPoint())
        .build();

    transactionInnerService.createTransferInTransaction(transferInInfo);

    return TransferResponse.builder()
        .amount(amount)
        .beforePoint(pointChangeDetail.getSenderBeforePoint())
        .afterPoint(pointChangeDetail.getSenderAfterPoint())
        .build();
  }

}
