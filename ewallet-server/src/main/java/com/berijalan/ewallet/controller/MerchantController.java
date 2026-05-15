package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResMerchantDto;
import com.berijalan.ewallet.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResMerchantDto>>> getAllMerchants() {
        List<ResMerchantDto> data = merchantService.getAllActiveMerchants();

        return ResponseEntity.ok(ApiResponseFactory.success("Merchants retrieved successfully", data));
    }
}
