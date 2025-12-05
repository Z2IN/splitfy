package org.zzin.splitfy.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.domain.auth.dto.request.SignupRequest;
import org.zzin.splitfy.domain.auth.dto.response.SignupResponse;
import org.zzin.splitfy.domain.auth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping
  public ResponseEntity<CommonResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    SignupResponse response = authService.signup(request);

    return CommonResponse.success(response);
  }
}
