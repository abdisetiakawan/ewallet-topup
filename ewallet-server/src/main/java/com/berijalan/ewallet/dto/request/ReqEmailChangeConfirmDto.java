package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload konfirmasi perubahan email.")
public record ReqEmailChangeConfirmDto(
        @Schema(description = "Token verifikasi yang dikirim ke email baru.", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "Token is required")
        String token
) {}
