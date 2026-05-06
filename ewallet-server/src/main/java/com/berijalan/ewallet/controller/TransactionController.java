package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<Object>> pay(
            @Valid @RequestBody ReqPayDto request,
            Principal principal) {
        
        try {
            //  email didapat dari JWT token yang login
            String userEmail = principal.getName(); 
            transactionService.pay(request, userEmail);
            
            BaseResponse<Object> response = new BaseResponse<>(
                    "req-" + UUID.randomUUID().toString().substring(0, 8),
                    true,
                    "Pembayaran berhasil",
                    null
            );
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            BaseResponse<Object> errorResponse = new BaseResponse<>(
                    "req-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            BaseResponse<Object> serverErrorResponse = new BaseResponse<>(
                    "req-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    "Terjadi kesalahan sistem",
                    null
            );
            return ResponseEntity.internalServerError().body(serverErrorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Principal principal) {
        
        try {
            String userEmail = principal.getName();
            ResTransactionHistoryDto data = transactionService.getTransactions(userEmail, page, size, status);
            
            BaseResponse<ResTransactionHistoryDto> response = new BaseResponse<>(
                    "req-" + UUID.randomUUID().toString().substring(0, 8),
                    true,
                    "Riwayat transaksi berhasil diambil",
                    data
            );
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            BaseResponse<ResTransactionHistoryDto> errorResponse = new BaseResponse<>(
                    "req-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}