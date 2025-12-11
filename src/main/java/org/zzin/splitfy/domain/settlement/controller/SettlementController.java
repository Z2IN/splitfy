package org.zzin.splitfy.domain.settlement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.settlement.dto.request.SettlementRequest;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementResponse;
import org.zzin.splitfy.domain.settlement.service.SettlementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
public class SettlementController {

  private final SettlementService settlementService;

  @PostMapping
  public CommonResponse<SettlementResponse> createSettlement(
      @AuthenticationPrincipal AuthUser authUser,
      @RequestBody SettlementRequest request
  ) {
    SettlementResponse response = settlementService.createSettlement(request);
    return CommonResponse.success(response);
  }

}

