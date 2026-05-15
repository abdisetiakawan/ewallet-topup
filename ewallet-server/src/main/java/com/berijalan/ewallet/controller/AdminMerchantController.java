package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.request.ReqAdminMerchantConfigDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResAdminMerchantDto;
import com.berijalan.ewallet.service.AdminMerchantService;
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
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResAdminMerchantDto>>> getAllMerchants() {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Admin merchants retrieved successfully",
                adminMerchantService.getAllMerchants()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Admin merchant retrieved successfully",
                adminMerchantService.getMerchant(id)
        ));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> createMerchant(
            @Valid @RequestBody ReqAdminMerchantConfigDto request
    ) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant created successfully",
                adminMerchantService.createMerchant(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResAdminMerchantDto>> updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody ReqAdminMerchantConfigDto request
    ) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant updated successfully",
                adminMerchantService.updateMerchant(id, request)
        ));
    }
}
