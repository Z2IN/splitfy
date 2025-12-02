package org.zzin.splitfy.domain.point.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.point.Service.PointService;
import org.zzin.splitfy.domain.point.dto.request.DepositRequest;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PointController {

  private final PointService pointService;

  @GetMapping("/users/points")
  @PreAuthorize("hasAuthentication()")
  public CommonResponse<Long> getUsersPoint(@PathVariable("id") long id,
      @AuthenticationPrincipal AuthUser authUser) {
    return CommonResponse.success(pointService.getPointBy(authUser.userId()));
  }

  @PostMapping("/deposit")
  public CommonResponse<DepositResponse> deposit(@Valid @RequestBody DepositRequest request,
      @AuthenticationPrincipal AuthUser authUser) {
    DepositResponse response = pointService.deposit(authUser.userId(), request.point());
    return CommonResponse.success(response);
  }

}
