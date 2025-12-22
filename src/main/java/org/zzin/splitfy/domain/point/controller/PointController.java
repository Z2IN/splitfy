package org.zzin.splitfy.domain.point.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.point.dto.request.DepositRequest;
import org.zzin.splitfy.domain.point.dto.request.TransferRequest;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.dto.response.TransferResponse;
import org.zzin.splitfy.domain.point.service.PointService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PointController {

  private final PointService pointService;

  @GetMapping("/users/points")
  public CommonResponse<Long> getUsersPoint(@AuthenticationPrincipal AuthUser authUser) {
    return CommonResponse.success(pointService.getPointBy(authUser));
  }

  @PostMapping("/deposit")
  public CommonResponse<DepositResponse> deposit(@Valid @RequestBody DepositRequest request,
      @AuthenticationPrincipal AuthUser authUser) {
    DepositResponse response = pointService.deposit(authUser, request.amount());
    return CommonResponse.success(response);
  }

  @PostMapping("/transfer")
  public CommonResponse<TransferResponse> transfer(@Valid @RequestBody TransferRequest request,
      @AuthenticationPrincipal AuthUser authUser) {
    TransferResponse response = pointService.transferTo(request.toUserId(), request.amount(),
        authUser);
    return CommonResponse.success(response);
  }

}
