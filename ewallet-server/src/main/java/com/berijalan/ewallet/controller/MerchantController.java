package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.config.OpenApiConfig;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchant", description = "Daftar merchant aktif yang tersedia untuk pembayaran.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(
            summary = "Melihat merchant aktif",
            description = "Mengambil daftar merchant aktif beserta konfigurasi pajaknya untuk kebutuhan pembayaran customer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daftar merchant aktif berhasil dikembalikan"),
            @ApiResponse(responseCode = "401", description = "Access token tidak valid atau tidak ada"),
            @ApiResponse(responseCode = "403", description = "Role pengguna tidak diizinkan mengakses merchant customer"),
            @ApiResponse(responseCode = "500", description = "Kesalahan internal server")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<ResMerchantDto>>> getAllMerchants() {
        List<ResMerchantDto> data = merchantService.getAllActiveMerchants();

        return ResponseEntity.ok(ApiResponseFactory.success("Merchants retrieved successfully", data));
    }
}
