package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReqEmailChangeConfirmDto(
        @NotBlank(message = "Token is required")
        String token
) {}
