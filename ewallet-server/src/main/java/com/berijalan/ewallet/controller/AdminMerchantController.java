package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.api.AdminMerchantApi;
import com.berijalan.ewallet.contract.model.BaseResponseResAdminMerchantDto;
import com.berijalan.ewallet.contract.model.BaseResponseResAdminMerchantDtoList;
import com.berijalan.ewallet.contract.model.ReqAdminMerchantConfigDto;
import com.berijalan.ewallet.service.AdminMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminMerchantController implements AdminMerchantApi {

    private final AdminMerchantService adminMerchantService;

    @Override
    public ResponseEntity<BaseResponseResAdminMerchantDtoList> getAllMerchants() {
        return ResponseEntity.ok(ApiResponseFactory.adminMerchantList(
                "Admin merchants retrieved successfully",
                adminMerchantService.getAllMerchants()
        ));
    }

    @Override
    public ResponseEntity<BaseResponseResAdminMerchantDto> getMerchant(Long id) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Admin merchant retrieved successfully",
                adminMerchantService.getMerchant(id)
        ));
    }

    @Override
    public ResponseEntity<BaseResponseResAdminMerchantDto> createMerchant(ReqAdminMerchantConfigDto request) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant created successfully",
                adminMerchantService.createMerchant(request)
        ));
    }

    @Override
    public ResponseEntity<BaseResponseResAdminMerchantDto> updateMerchant(Long id, ReqAdminMerchantConfigDto request) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Merchant updated successfully",
                adminMerchantService.updateMerchant(id, request)
        ));
    }
}
