package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<ResPaymentDto>> pay(
            @Valid @RequestBody ReqPayDto request,
            Principal principal) {
        String userEmail = getAuthenticatedEmail(principal);
        ResPaymentDto data = transactionService.pay(request, userEmail);

        return ResponseEntity.ok(new BaseResponse<>(
                generateRequestId(),
                true,
                "Payment successful",
                data
        ));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size minimum is 1") @Max(value = 100, message = "Size maximum is 100") int size,
            @RequestParam(required = false) String status,
            Principal principal) {
        String userEmail = getAuthenticatedEmail(principal);
        ResTransactionHistoryDto data = transactionService.getTransactions(userEmail, page, size, status);

        return ResponseEntity.ok(new BaseResponse<>(
                generateRequestId(),
                true,
                "Transaction history retrieved successfully",
                data
        ));
    }

    private String getAuthenticatedEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return principal.getName();
    }

    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
