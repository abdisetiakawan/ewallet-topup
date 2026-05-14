package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements BaseMapper<User, ResUserSummaryDto> {

    @Override
    public ResUserSummaryDto toDto(User user) {
        return new ResUserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }

    public ResUserSummaryDto toSummaryDto(User user) {
        return toDto(user);
    }

    public ResLoginDto toLoginDto(String accessToken, long expiresIn, User user) {
        return new ResLoginDto(
                accessToken,
                "Bearer",
                expiresIn,
                toSummaryDto(user)
        );
    }
}
