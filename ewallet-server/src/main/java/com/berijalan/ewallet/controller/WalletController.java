package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
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
        String email = authentication.getName();
        ResWalletBalanceDto data = walletService.getBalance(email);

        BaseResponse<ResWalletBalanceDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Saldo berhasil diambil",
                data
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/topup")
    public ResponseEntity<BaseResponse<ResTopupDto>> topup(@Valid @RequestBody ReqTopupDto request, Authentication authentication) {
        String email = authentication.getName();
        ResTopupDto data = walletService.topup(request, email);

        BaseResponse<ResTopupDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Top-up berhasil",
                data
        );
        return ResponseEntity.ok(response);
    }
}
