package org.zzin.splitfy.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zzin.splitfy.common.dto.CommonResponse;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 응답 생성 및 로깅
    private ResponseEntity<CommonResponse<?>> buildResponseEntity(HttpStatus status, String message,
                                                                  Exception ex) {
        log.error("Exception handled [{}]: {}", status, ex.getMessage(), ex);
        return ResponseEntity.status(status).body(CommonResponse.failure(message));
    }

    // Validation 관련 예외 (Request body, DTO validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonResponse<?>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<?>> handleConstraintViolation(
            ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex);
    }

    // 요청 파싱/파라미터 관련
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<?>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        String message = ex.getParameterName() + " parameter is missing";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        String message = "Malformed request body";
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex);
    }

    // 권한 / 인증
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponseEntity(HttpStatus.FORBIDDEN, "Access denied", ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse<?>> handleAuthentication(AuthenticationException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, "Authentication failed", ex);
    }

    // HTTP method / media type
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<?>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        String message = "Method " + ex.getMethod() + " is not supported for this endpoint";
        return buildResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, message, ex);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CommonResponse<?>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {
        return buildResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media type not supported", ex);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<?>> handleBusinessException(BusinessException ex) {
        return buildResponseEntity(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getMessage(),
                ex
        );
    }

    // Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleAll(Exception ex) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex);
    }
}
