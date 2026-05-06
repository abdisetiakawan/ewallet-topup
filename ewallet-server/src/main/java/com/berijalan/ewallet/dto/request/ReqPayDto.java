package com.berijalan.ewallet.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReqPayDto(
        @NotBlank(message = "Nama merchant tidak boleh kosong")
        String merchantName,

        @NotNull(message = "Nominal pembayaran wajib diisi")
        @Min(value = 1, message = "Minimal pembayaran adalah 1")
        Long amount
) {}