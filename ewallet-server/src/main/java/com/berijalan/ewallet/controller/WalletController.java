package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.contract.api.WalletApi;
import com.berijalan.ewallet.contract.model.BaseResponseResTopupDto;
import com.berijalan.ewallet.contract.model.BaseResponseResWalletBalanceDto;
import com.berijalan.ewallet.contract.model.ReqTopupDto;
import com.berijalan.ewallet.contract.model.ResTopupDto;
import com.berijalan.ewallet.contract.model.ResWalletBalanceDto;
import com.berijalan.ewallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public ResponseEntity<BaseResponseResWalletBalanceDto> getBalance() {
        Long userId = CurrentUser.id(SecurityContextHolder.getContext().getAuthentication());
        ResWalletBalanceDto data = walletService.getBalance(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Balance retrieved successfully", data));
    }

    @Override
    @IdempotencyGuarded
    public ResponseEntity<BaseResponseResTopupDto> topup(String idempotencyKey, ReqTopupDto request) {
        Long userId = CurrentUser.id(SecurityContextHolder.getContext().getAuthentication());
        ResTopupDto data = walletService.topup(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Top-up successful", data));
    }
}
