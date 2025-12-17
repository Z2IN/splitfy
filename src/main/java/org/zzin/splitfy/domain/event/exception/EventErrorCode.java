package org.zzin.splitfy.domain.event.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {
  EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트가 존재하지 않습니다."),

  PAST_START_TIME(HttpStatus.BAD_REQUEST, "이벤트 시작 시간은 현재 시간 이후여야 합니다."),
  INVALID_EVENT_TIME(HttpStatus.BAD_REQUEST, "이벤트 종료 시간은 시작 시간 이후여야 합니다."),

  INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고는 1개 이상이어야 합니다."),
  STOCK_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "재고 설정 한도를 초과했습니다."),

  EVENT_NOT_STARTED(HttpStatus.BAD_REQUEST, "이벤트가 시작되지 않았습니다."),
  EVENT_ENDED(HttpStatus.BAD_REQUEST, "종료된 이벤트 입니다."),

  ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "이미 대기열에 참여 중입니다."),
  ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "이미 참여한 이벤트입니다."),

  NOT_IN_QUEUE(HttpStatus.NOT_FOUND, "대기열 참여 내역이 없습니다."),

  NOT_YOUR_TURN(HttpStatus.BAD_REQUEST, "차례가 아닙니다."),

  NUMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 번호입니다."),
  NUMBER_ALREADY_TAKEN(HttpStatus.BAD_REQUEST, "이미 선택된 번호입니다."),


  INVALID_CURSOR(HttpStatus.BAD_REQUEST, "유효하지 않은 커서 값입니다."),

  ;

  private final HttpStatus status;
  private final String message;
}
