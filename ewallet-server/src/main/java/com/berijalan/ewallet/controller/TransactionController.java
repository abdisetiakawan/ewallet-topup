package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @IdempotencyGuarded
    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<ResPaymentDto>> pay(
            @Valid @RequestBody ReqPayDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResPaymentDto data = transactionService.pay(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Payment successful", data));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @Valid @ModelAttribute ReqTransactionHistoryDto request,
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResTransactionHistoryDto data = transactionService.getTransactions(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Transaction history retrieved successfully", data));
    }
}
