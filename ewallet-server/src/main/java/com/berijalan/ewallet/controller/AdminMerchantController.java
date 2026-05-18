package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.OpenApiConfig;
import com.berijalan.ewallet.dto.request.ReqAdminMerchantConfigDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.service.AdminMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "Admin Merchant", description = "Manajemen merchant dan konfigurasi pajak oleh admin.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @Operation(
            summary = "Melihat semua merchant",
            description = "Mengambil seluruh merchant untuk kebutuhan administrasi, termasuk merchant yang tidak aktif."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daftar merchant admin berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna bukan ADMIN"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<ResAdminMerchantDto>>> getAllMerchants() {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Admin merchants retrieved successfully",
                adminMerchantService.getAllMerchants()
        ));
    }

    @Operation(
            summary = "Melihat detail merchant",
            description = "Mengambil detail merchant dan pajaknya berdasarkan ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detail merchant berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna bukan ADMIN"),
            @ApiResponse(responseCode = "404", description = "Merchant tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> getMerchant(
            @Parameter(description = "ID merchant yang akan diambil.") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Admin merchant retrieved successfully",
                adminMerchantService.getMerchant(id)
        ));
    }

    @Operation(
            summary = "Membuat merchant",
            description = "Membuat merchant baru beserta daftar konfigurasi pajak awalnya."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Merchant berhasil dibuat"),
            @ApiResponse(responseCode = "400", description = "Payload merchant atau pajak tidak valid"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna bukan ADMIN"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> createMerchant(
            @Valid @RequestBody ReqAdminMerchantConfigDto request
    ) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant created successfully",
                adminMerchantService.createMerchant(request)
        ));
    }

    @Operation(
            summary = "Memperbarui merchant",
            description = "Memperbarui data merchant dan melakukan upsert daftar pajaknya dalam satu transaksi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Merchant berhasil diperbarui"),
            @ApiResponse(responseCode = "400", description = "Payload tidak valid atau pajak tidak dimiliki merchant"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna bukan ADMIN"),
            @ApiResponse(responseCode = "404", description = "Merchant tidak ditemukan"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> updateMerchant(
            @Parameter(description = "ID merchant yang akan diperbarui.") @PathVariable Long id,
            @Valid @RequestBody ReqAdminMerchantConfigDto request
    ) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant updated successfully",
                adminMerchantService.updateMerchant(id, request)
        ));
    }
}
