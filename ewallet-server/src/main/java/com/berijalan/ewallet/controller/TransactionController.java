package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.UnauthorizedException;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        String userEmail = getAuthenticatedEmail(authentication);
        ResPaymentDto data = transactionService.pay(request, userEmail);

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
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "10") String size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        int pageNumber = parsePage(page);
        int pageSize = parseSize(size);

        String userEmail = getAuthenticatedEmail(authentication);
        ResTransactionHistoryDto data = transactionService.getTransactions(userEmail, pageNumber, pageSize, status);

        BaseResponse<ResTransactionHistoryDto> response = new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Transaction history retrieved successfully",
                data
        );
        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new UnauthorizedException("Authentication is required");
        }
        return authentication.getName();
    }

    private int parsePage(String page) {
        int pageNumber = parseInteger(page, "Page must be a number");
        if (pageNumber < 0) {
            throw new BadRequestException("Page must not be negative");
        }
        return pageNumber;
    }

    private int parseSize(String size) {
        int pageSize = parseInteger(size, "Size must be a number");
        if (pageSize < 1) {
            throw new BadRequestException("Size minimum is 1");
        }
        if (pageSize > 100) {
            throw new BadRequestException("Size maximum is 100");
        }
        return pageSize;
    }

    private int parseInteger(String value, String message) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(message);
        }
    }
}
