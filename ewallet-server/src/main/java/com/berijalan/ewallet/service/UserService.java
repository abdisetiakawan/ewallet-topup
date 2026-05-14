package com.berijalan.ewallet.service;

import com.berijalan.ewallet.dto.request.ReqChangePasswordDto;
import com.berijalan.ewallet.dto.request.ReqUpdateProfileDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.entity.User;
import com.berijalan.ewallet.exception.BadRequestException;
import com.berijalan.ewallet.exception.NotFoundException;
import com.berijalan.ewallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResUserSummaryDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toDto(user);
    }

    @Transactional
    public ResUserSummaryDto updateProfile(Long userId, ReqUpdateProfileDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setName(request.name());
        userRepository.save(user);

        return toDto(user);
    }

    private ResUserSummaryDto toDto(User user) {
        return new ResUserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getRole().name()
        );
    }

    @Transactional
    public void changePassword(Long userId, ReqChangePasswordDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Password lama tidak sesuai");
        }

        if (request.oldPassword().equals(request.newPassword())) {
            throw new BadRequestException("Password baru tidak boleh sama dengan password lama");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for userId={}", userId);
    }
}
