package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.contract.api.TransaksiApi;
import com.berijalan.ewallet.contract.model.BaseResponseResPaymentDto;
import com.berijalan.ewallet.contract.model.BaseResponseResPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.BaseResponseResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.BaseResponseResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.ReqPayDto;
import com.berijalan.ewallet.contract.model.ReqPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.ResPaymentDto;
import com.berijalan.ewallet.contract.model.ResPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.ResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.ResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.TransactionStatus;
import com.berijalan.ewallet.contract.model.TransactionType;
import com.berijalan.ewallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransaksiApi {

    private final TransactionService transactionService;

    @Override
    @IdempotencyGuarded
    public ResponseEntity<BaseResponseResPaymentDto> pay(String idempotencyKey, ReqPayDto request) {
        Long userId = currentUserId();
        ResPaymentDto data = transactionService.pay(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Payment successful", data));
    }

    @Override
    public ResponseEntity<BaseResponseResPaymentQuoteDto> quotePayment(ReqPaymentQuoteDto request) {
        ResPaymentQuoteDto data = transactionService.quotePayment(request);

        return ResponseEntity.ok(ApiResponseFactory.success("Payment quote calculated successfully", data));
    }

    @Override
    public ResponseEntity<BaseResponseResTransactionHistoryDto> getTransactions(
            Integer page,
            Integer size,
            TransactionStatus status,
            TransactionType type
    ) {
        Long userId = currentUserId();
        ResTransactionHistoryDto data = transactionService.getTransactions(userId, page, size, status, type);

        return ResponseEntity.ok(ApiResponseFactory.success("Transaction history retrieved successfully", data));
    }

    @Override
    public ResponseEntity<BaseResponseResTransactionDetailDto> getTransaction(Long transactionId) {
        Long userId = currentUserId();
        ResTransactionDetailDto data = transactionService.getTransaction(userId, transactionId);

        return ResponseEntity.ok(ApiResponseFactory.success("Transaction detail retrieved successfully", data));
    }

    private Long currentUserId() {
        return CurrentUser.id(SecurityContextHolder.getContext().getAuthentication());
    }
}
