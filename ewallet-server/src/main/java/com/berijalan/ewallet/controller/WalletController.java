package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.security.CurrentUser;
import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.IdempotencyGuarded;
import com.berijalan.ewallet.config.OpenApiConfig;
import com.berijalan.ewallet.dto.request.ReqTopupDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResTopupDto;
import com.berijalan.ewallet.dto.response.ResWalletBalanceDto;
import com.berijalan.ewallet.service.WalletService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Saldo dan mutasi top-up wallet customer.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class WalletController {

    private final WalletService walletService;

    @Operation(
            summary = "Melihat saldo wallet",
            description = "Mengambil saldo terakhir milik customer yang sedang login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses wallet"),
            @ApiResponse(responseCode = "404", description = "Wallet customer tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping("/balance")
    public ResponseEntity<BaseResponse<ResWalletBalanceDto>> getBalance(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResWalletBalanceDto data = walletService.getBalance(userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Balance retrieved successfully", data));
    }

    @Operation(
            summary = "Top-up wallet",
            description = "Menambah saldo wallet customer dan mencatat transaksi top-up yang idempoten."
    )
    @Parameter(
            name = "Idempotency-Key",
            in = ParameterIn.HEADER,
            required = true,
            description = "Kunci unik per request top-up untuk mencegah transaksi finansial ganda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top-up berhasil"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid, amount di luar batas, atau header Idempotency-Key tidak ada"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses wallet"),
            @ApiResponse(responseCode = "404", description = "Wallet customer tidak ditemukan"),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key sedang diproses atau dipakai untuk request berbeda"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @IdempotencyGuarded
    @PostMapping("/topup")
    public ResponseEntity<BaseResponse<ResTopupDto>> topup(
            @Valid @RequestBody ReqTopupDto request,
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        ResTopupDto data = walletService.topup(request, userId);

        return ResponseEntity.ok(ApiResponseFactory.success("Top-up successful", data));
    }
}
