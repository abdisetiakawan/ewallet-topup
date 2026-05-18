package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload login customer atau admin.")
public record ReqLoginDto(
        @Schema(description = "Email akun yang akan diautentikasi.", example = "budi@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Password akun.", example = "Password1")
        @NotBlank(message = "Password is required")
        String password
) {}
