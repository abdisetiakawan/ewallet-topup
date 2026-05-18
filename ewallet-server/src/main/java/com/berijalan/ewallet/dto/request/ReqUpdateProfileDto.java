package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload perubahan profil customer.")
public record ReqUpdateProfileDto(
        @Schema(description = "Nama baru customer.", example = "Budi Santoso", maxLength = 100)
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name
) {}
