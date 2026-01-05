package org.zzin.splitfy.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  HttpStatus getStatus();

  String getMessage();
}
