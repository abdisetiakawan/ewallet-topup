package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Envelope standar seluruh response API.")
public record BaseResponse<T>(
        @Schema(description = "Request ID untuk korelasi log dan troubleshooting.", example = "b6a9f2f0a0e04a1bb0c1")
        String requestId,
        @Schema(description = "Status keberhasilan response.", example = "true")
        boolean status,
        @Schema(description = "Pesan ringkas hasil operasi.", example = "Payment successful")
        String message,
        @Schema(description = "Payload response. Tipe data berbeda sesuai endpoint.")
        T data
) {}
