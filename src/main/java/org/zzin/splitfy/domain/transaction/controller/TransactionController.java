package org.zzin.splitfy.domain.transaction.controller;

import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonPage;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.transaction.dto.response.GetTransactionsByResponse;
import org.zzin.splitfy.domain.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class TransactionController {

  private final TransactionService transactionService;

  @GetMapping("/transactions")
  public CommonResponse<CommonPage<GetTransactionsByResponse>> getTransactionsBy(
      @AuthenticationPrincipal AuthUser authUser, @PageableDefault Pageable pageable) {

    var pageResult = transactionService.getTransactionsBy(
        authUser, pageable.getPageNumber(), pageable.getPageSize());
    Page<GetTransactionsByResponse> response = pageResult.map(GetTransactionsByResponse::fromDto);

    return CommonResponse.success(CommonPage.of(response));
  }

}
