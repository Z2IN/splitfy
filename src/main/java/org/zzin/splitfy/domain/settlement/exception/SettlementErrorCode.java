package org.zzin.splitfy.domain.settlement.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

  DUPLICATE_ALLOCATION_TARGETS(HttpStatus.BAD_REQUEST,
      "정산 대상자 목록에 중복된 사용자가 있습니다."), SELF_ALLOCATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
          "결제자는 정산 대상자에 포함되어야 합니다."), EMPTY_PAYMENT_REQUESTS(HttpStatus.BAD_REQUEST,
              "결제 요청이 존재하지 않습니다."), INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST,
                  "결제 금액이 0 이하일 수 없습니다."), EMPTY_ALLOCATION_TARGETS(HttpStatus.BAD_REQUEST,
                      "정산 대상자가 존재하지 않습니다."), SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND,
                          "해당 정산 내역을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;
}
