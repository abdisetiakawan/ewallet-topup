package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload permintaan perubahan email.")
public record ReqEmailChangeDto(
        @Schema(description = "Email baru yang akan diverifikasi sebelum disimpan.", example = "budi.baru@example.com")
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        String newEmail
) {}
