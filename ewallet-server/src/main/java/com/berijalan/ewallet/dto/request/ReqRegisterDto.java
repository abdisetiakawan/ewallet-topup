package com.berijalan.ewallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload registrasi customer.")
public record ReqRegisterDto(
        @Schema(description = "Nama customer.", example = "Budi Santoso")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Email unik yang digunakan untuk login.", example = "budi@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Password 8 sampai 72 karakter, minimal satu huruf besar dan satu angka.", example = "Password1")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z]).*$", message = "Password must contain at least one uppercase letter and one number")
        String password
) {}
