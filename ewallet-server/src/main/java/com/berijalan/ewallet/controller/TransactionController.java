package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<Void>> pay(
            @Valid @RequestBody ReqPayDto request,
            Principal principal) {

        transactionService.pay(request, principal.getName());

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Payment successful",
                null
        ));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Principal principal) {

        ResTransactionHistoryDto data = transactionService.getTransactions(
                principal.getName(), page, size, status);

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Transaction history retrieved successfully",
                data
        ));
    }
}
