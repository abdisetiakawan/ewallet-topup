package com.berijalan.ewallet.controller;

import com.berijalan.ewallet.config.MdcFilter;
import com.berijalan.ewallet.dto.request.ReqChangePasswordDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeConfirmDto;
import com.berijalan.ewallet.dto.request.ReqEmailChangeDto;
import com.berijalan.ewallet.dto.request.ReqUpdateProfileDto;
import com.berijalan.ewallet.dto.response.BaseResponse;
import com.berijalan.ewallet.dto.response.ResEmailChangeDto;
import com.berijalan.ewallet.dto.response.ResUserSummaryDto;
import com.berijalan.ewallet.security.UserDetailsImpl;
import com.berijalan.ewallet.service.EmailChangeService;
import com.berijalan.ewallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailChangeService emailChangeService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> getProfile(Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        ResUserSummaryDto data = userService.getProfile(userId);

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Profile retrieved successfully",
                data
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> updateProfile(
            @Valid @RequestBody ReqUpdateProfileDto request,
            Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        ResUserSummaryDto data = userService.updateProfile(userId, request);

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Profile updated successfully",
                data
        ));
    }

    @PostMapping("/me/email")
    public ResponseEntity<BaseResponse<ResEmailChangeDto>> requestEmailChange(
            @Valid @RequestBody ReqEmailChangeDto request,
            Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        ResEmailChangeDto data = emailChangeService.requestEmailChange(userId, request.newEmail());

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Verification token has been sent to " + request.newEmail(),
                data
        ));
    }

    @PostMapping("/me/email/confirm")
    public ResponseEntity<BaseResponse<ResUserSummaryDto>> confirmEmailChange(
            @Valid @RequestBody ReqEmailChangeConfirmDto request,
            Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        ResUserSummaryDto data = emailChangeService.confirmEmailChange(userId, request.token());

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Email updated successfully",
                data
        ));
    }

    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody ReqChangePasswordDto request,
            Authentication authentication) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        userService.changePassword(userId, request);

        return ResponseEntity.ok(new BaseResponse<>(
                MDC.get(MdcFilter.REQUEST_ID),
                true,
                "Password berhasil diubah",
                null
        ));
    }
}
