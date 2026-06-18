package com.berijalan.ewallet.common.web;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.contract.model.BaseResponseErrorMap;
import com.berijalan.ewallet.contract.model.BaseResponseResAdminMerchantDto;
import com.berijalan.ewallet.contract.model.BaseResponseResAdminMerchantDtoList;
import com.berijalan.ewallet.contract.model.BaseResponseResEmailChangeDto;
import com.berijalan.ewallet.contract.model.BaseResponseResLoginDto;
import com.berijalan.ewallet.contract.model.BaseResponseResMerchantDtoList;
import com.berijalan.ewallet.contract.model.BaseResponseResPaymentDto;
import com.berijalan.ewallet.contract.model.BaseResponseResPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.BaseResponseResRefreshTokenDto;
import com.berijalan.ewallet.contract.model.BaseResponseResTopupDto;
import com.berijalan.ewallet.contract.model.BaseResponseResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.BaseResponseResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.BaseResponseResUserSummaryDto;
import com.berijalan.ewallet.contract.model.BaseResponseResWalletBalanceDto;
import com.berijalan.ewallet.contract.model.BaseResponseVoid;
import com.berijalan.ewallet.contract.model.ResAdminMerchantDto;
import com.berijalan.ewallet.contract.model.ResEmailChangeDto;
import com.berijalan.ewallet.contract.model.ResLoginDto;
import com.berijalan.ewallet.contract.model.ResMerchantDto;
import com.berijalan.ewallet.contract.model.ResPaymentDto;
import com.berijalan.ewallet.contract.model.ResPaymentQuoteDto;
import com.berijalan.ewallet.contract.model.ResRefreshTokenDto;
import com.berijalan.ewallet.contract.model.ResTopupDto;
import com.berijalan.ewallet.contract.model.ResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.ResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.ResUserSummaryDto;
import com.berijalan.ewallet.contract.model.ResWalletBalanceDto;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static BaseResponseVoid success(String message) {
        return new BaseResponseVoid(requestId(), true, message);
    }

    public static BaseResponseResUserSummaryDto success(String message, ResUserSummaryDto data) {
        return new BaseResponseResUserSummaryDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResLoginDto success(String message, ResLoginDto data) {
        return new BaseResponseResLoginDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResRefreshTokenDto success(String message, ResRefreshTokenDto data) {
        return new BaseResponseResRefreshTokenDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResWalletBalanceDto success(String message, ResWalletBalanceDto data) {
        return new BaseResponseResWalletBalanceDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResTopupDto success(String message, ResTopupDto data) {
        return new BaseResponseResTopupDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResPaymentDto success(String message, ResPaymentDto data) {
        return new BaseResponseResPaymentDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResPaymentQuoteDto success(String message, ResPaymentQuoteDto data) {
        return new BaseResponseResPaymentQuoteDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResTransactionHistoryDto success(String message, ResTransactionHistoryDto data) {
        return new BaseResponseResTransactionHistoryDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResTransactionDetailDto success(String message, ResTransactionDetailDto data) {
        return new BaseResponseResTransactionDetailDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResEmailChangeDto success(String message, ResEmailChangeDto data) {
        return new BaseResponseResEmailChangeDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResAdminMerchantDto success(String message, ResAdminMerchantDto data) {
        return new BaseResponseResAdminMerchantDto(requestId(), true, message).data(data);
    }

    public static BaseResponseResMerchantDtoList merchantList(String message, List<ResMerchantDto> data) {
        return new BaseResponseResMerchantDtoList(requestId(), true, message).data(data);
    }

    public static BaseResponseResAdminMerchantDtoList adminMerchantList(String message, List<ResAdminMerchantDto> data) {
        return new BaseResponseResAdminMerchantDtoList(requestId(), true, message).data(data);
    }

    public static BaseResponseVoid error(String message) {
        return new BaseResponseVoid(requestId(), false, message);
    }

    public static BaseResponseErrorMap validationError(String message, Map<String, String> data) {
        return new BaseResponseErrorMap(requestId(), false, message).data(data);
    }

    public static String requestId() {
        String requestId = MDC.get(MdcFilter.REQUEST_ID);
        // WHY: Nilai cadangan menjaga response tetap punya correlation ID meski dipanggil di luar servlet filter.
        return requestId != null ? requestId : "req-" + UUID.randomUUID();
    }
}
