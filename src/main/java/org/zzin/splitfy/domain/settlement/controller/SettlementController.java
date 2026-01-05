package org.zzin.splitfy.domain.settlement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonPage;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementHistoryResponse;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.service.SettlementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
@PreAuthorize("isAuthenticated()")
public class SettlementController {

  private final SettlementService settlementService;

  @PostMapping
  public CommonResponse<SettlementResponse> createSettlement(
      @AuthenticationPrincipal AuthUser authUser,
      @Valid @RequestBody SettlementRequest request
  ) {
    SettlementResponse response = settlementService.createSettlement(authUser, request);
    return CommonResponse.success(response);
  }

  @GetMapping("/me")
  public CommonResponse<CommonPage<SettlementHistoryResponse>> getSettlementHistory(
      @PageableDefault(size = 10, page = 0) Pageable pageable,
      @AuthenticationPrincipal AuthUser authUser) {
    CommonPage<SettlementHistoryResponse> response = settlementService.getSettlementHistory(
        pageable, authUser);
    return CommonResponse.success(response);
  }
}
