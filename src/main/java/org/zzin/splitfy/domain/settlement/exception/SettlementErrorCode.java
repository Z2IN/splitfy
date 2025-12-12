package org.zzin.splitfy.domain.settlement.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {
  DUPLICATE_ALLOCATION_TARGETS(HttpStatus.BAD_REQUEST, "정산 대상자 목록에 중복된 사용자가 있습니다."),
  INVALID_USER_ID(HttpStatus.BAD_REQUEST, "정산 대상자 중 유효하지 않은 사용자 ID가 포함되어 있습니다."),
  SELF_ALLOCATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "결제자는 정산 대상자에 포함되어야 합니다.");

  private final HttpStatus status;
  private final String message;
}
