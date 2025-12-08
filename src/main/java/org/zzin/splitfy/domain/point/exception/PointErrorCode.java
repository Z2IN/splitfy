package org.zzin.splitfy.domain.point.exception;

import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointErrorCode implements ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."), NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST,
      "포인트가 부족합니다.");

  private final HttpStatus status;
  private final String message;
}