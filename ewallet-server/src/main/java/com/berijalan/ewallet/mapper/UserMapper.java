package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.contract.model.ResLoginDto;
import com.berijalan.ewallet.contract.model.ResUserSummaryDto;
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
        return new ResUserSummaryDto()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name());
    }

    public ResLoginDto toLoginDto(String accessToken, long expiresIn, UserDetailsImpl user) {
        return new ResLoginDto()
                .token(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(toSummaryDtoFromPrincipal(user));
    }

    private ResUserSummaryDto toSummaryDtoFromPrincipal(UserDetailsImpl user) {
        return new ResUserSummaryDto()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name());
    }
}
