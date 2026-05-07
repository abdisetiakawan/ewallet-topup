package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
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

    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<ResPaymentDto>> pay(
            @Valid @RequestBody ReqPayDto request,
            Authentication authentication) {
        ResPaymentDto data = transactionService.pay(request, authentication.getName());

        BaseResponse<ResPaymentDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Payment successful",
                data
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @Valid @ModelAttribute ReqTransactionHistoryDto request,
            Authentication authentication) {
        ResTransactionHistoryDto data = transactionService.getTransactions(authentication.getName(), request);

        BaseResponse<ResTransactionHistoryDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Transaction history retrieved successfully",
                data
        );
        return ResponseEntity.ok(response);
    }
}
