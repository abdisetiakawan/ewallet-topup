package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.common.web.ApiResponseFactory;
import com.berijalan.ewallet.contract.api.MerchantApi;
import com.berijalan.ewallet.contract.model.BaseResponseResMerchantDtoList;
import com.berijalan.ewallet.contract.model.ResMerchantDto;
import com.berijalan.ewallet.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MerchantController implements MerchantApi {

    private final MerchantService merchantService;

    @Override
    public ResponseEntity<BaseResponseResMerchantDtoList> getAllActiveMerchants() {
        List<ResMerchantDto> data = merchantService.getAllActiveMerchants();

        return ResponseEntity.ok(ApiResponseFactory.merchantList("Merchants retrieved successfully", data));
    }
}
