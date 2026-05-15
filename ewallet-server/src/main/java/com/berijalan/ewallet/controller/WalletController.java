package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<BaseResponse<ResWalletBalanceDto>> getBalance(Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResWalletBalanceDto data = walletService.getBalance(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Balance retrieved successfully", data));
    }

    @IdempotencyGuarded
    @PostMapping("/topup")
    public ResponseEntity<BaseResponse<ResTopupDto>> topup(@Valid @RequestBody ReqTopupDto request, Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResTopupDto data = walletService.topup(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Top-up successful", data));
    }
}
