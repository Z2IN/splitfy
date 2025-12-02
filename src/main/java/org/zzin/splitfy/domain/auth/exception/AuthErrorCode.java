package org.zzin.splitfy.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  PASSWORD_ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 암호화에 실패하였습니다."),

  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
  DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");

  private final HttpStatus status;
  private final String message;
}
