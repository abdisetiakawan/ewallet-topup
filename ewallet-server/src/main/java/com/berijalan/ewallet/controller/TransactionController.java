package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.config.OpenApiConfig;
import com.berijalan.ewallet.dto.request.ReqPayDto;
import com.berijalan.ewallet.dto.request.ReqTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionDetailDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaksi", description = "Pembayaran merchant dan riwayat transaksi customer.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Membayar merchant",
            description = "Mengurangi saldo wallet customer, menghitung pajak aktif merchant, dan menyimpan snapshot transaksi."
    )
    @Parameter(
            name = "Idempotency-Key",
            in = ParameterIn.HEADER,
            required = true,
            description = "Kunci unik per request pembayaran untuk mencegah pemotongan saldo ganda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pembayaran berhasil"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid, saldo tidak cukup, amount di luar batas, atau header Idempotency-Key tidak ada"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses transaksi"),
            @ApiResponse(responseCode = "404", description = "Wallet atau merchant tidak ditemukan"),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key sedang diproses atau dipakai untuk request berbeda"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @IdempotencyGuarded
    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<ResPaymentDto>> pay(
            @Valid @RequestBody ReqPayDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResPaymentDto data = transactionService.pay(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Payment successful", data));
    }

    @Operation(
            summary = "Melihat riwayat transaksi",
            description = "Mengambil riwayat transaksi customer dengan filter status, tipe, dan pagination."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Riwayat transaksi berhasil dikembalikan"),
            @ApiResponse(responseCode = "400", description = "Query parameter tidak valid atau tipe enum tidak dikenal"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses transaksi"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<ResTransactionHistoryDto>> getTransactions(
            @Valid @ModelAttribute ReqTransactionHistoryDto request,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResTransactionHistoryDto data = transactionService.getTransactions(userId, request);

        return ResponseEntity.ok(ApiResponseFactory.success("Transaction history retrieved successfully", data));
    }

    @Operation(
            summary = "Melihat detail transaksi",
            description = "Mengambil detail transaksi customer beserta snapshot pajak pembayaran untuk transaksi miliknya."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detail transaksi berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses transaksi"),
            @ApiResponse(responseCode = "404", description = "Transaksi tidak ditemukan atau bukan milik customer"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping("/{transactionId}")
    public ResponseEntity<BaseResponse<ResTransactionDetailDto>> getTransaction(
            @PathVariable Long transactionId,
            @Parameter(hidden = true)
            Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResTransactionDetailDto data = transactionService.getTransaction(userId, transactionId);

        return ResponseEntity.ok(ApiResponseFactory.success("Transaction detail retrieved successfully", data));
    }
}
