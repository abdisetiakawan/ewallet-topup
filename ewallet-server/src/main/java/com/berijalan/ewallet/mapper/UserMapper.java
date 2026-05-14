package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResLoginDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.security.UserDetailsImpl;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements BaseMapper<User, ResUserSummaryDto> {

    @Override
    public ResUserSummaryDto toDto(User user) {
        return toSummaryDto(user);
    }

    public ResUserSummaryDto toSummaryDto(User user) {
        return new ResUserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }

    public ResLoginDto toLoginDto(String accessToken, long expiresIn, UserDetailsImpl user) {
        return new ResLoginDto(
                accessToken,
                "Bearer",
                expiresIn,
                toSummaryDtoFromPrincipal(user)
        );
    }

    private ResUserSummaryDto toSummaryDtoFromPrincipal(UserDetailsImpl user) {
        return new ResUserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }
}
