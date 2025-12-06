package org.zzin.splitfy.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  PASSWORD_ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 암호화에 실패하였습니다."),

  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

  INVALID_EMPTY_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일과 비밀번호는 공백일 수 없습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");

  private final HttpStatus status;
  private final String message;
}
