package org.zzin.splitfy.domain.transaction.exception;

import org.springframework.http.HttpStatus;
import org.zzin.splitfy.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionErrorCode implements ErrorCode {

  TRANSACTION_CREATION_FAILED("트랜잭션 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),;

  private final String message;
  private final HttpStatus status;
}
