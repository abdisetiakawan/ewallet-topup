package com.berijalan.ewallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ringkasan data pengguna.")
public record ResUserSummaryDto(
        @Schema(description = "ID pengguna.", example = "1")
        Long userId,
        @Schema(description = "Nama pengguna.", example = "Budi Santoso")
        String name,
        @Schema(description = "Email pengguna.", example = "budi@example.com")
        String email,
        @Schema(description = "Waktu akun dibuat.", example = "2026-05-18T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Role pengguna.", example = "CUSTOMER")
        String role
) {}
