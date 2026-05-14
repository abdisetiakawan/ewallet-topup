package com.berijalan.ewallet.dto.response;

import com.berijalan.ewallet.entity.User;
import java.time.LocalDateTime;

public record ResUserSummaryDto(
        Long userId,
        String name,
        String email,
        LocalDateTime createdAt,
        String role
) {
    public static ResUserSummaryDto from(User user) {
        return new ResUserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }
}
