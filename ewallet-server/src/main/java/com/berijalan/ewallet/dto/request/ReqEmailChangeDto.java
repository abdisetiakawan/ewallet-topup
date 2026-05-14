package com.berijalan.ewallet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqEmailChangeDto(
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        String newEmail
) {}
