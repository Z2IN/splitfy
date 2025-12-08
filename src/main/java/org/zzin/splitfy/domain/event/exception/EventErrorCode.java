package org.zzin.splitfy.domain.event.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {
  PAST_START_TIME(HttpStatus.BAD_REQUEST, "이벤트 시작 시간은 현재 시간 이후여야 합니다."),
  INVALID_EVENT_TIME(HttpStatus.BAD_REQUEST, "이벤트 종료 시간은 시작 시간 이후여야 합니다."),

  INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고는 1개 이상이어야 합니다."),
  STOCK_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "재고 설정 한도를 초과했습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
