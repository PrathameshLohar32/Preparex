package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.OtpSendRequestDto;
import com.preparex.preparex_backend.exception.InvalidCredentialsException;
import com.preparex.preparex_backend.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * OTP management endpoints for sending and resending OTPs.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/otp")
@RequiredArgsConstructor
@Tag(name = "OTP", description = "OTP send and resend APIs")
public class OtpController {

    private final OtpService otpService;

    @Operation(summary = "Send OTP", description = "Sends an OTP to the provided phone number or email address")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody OtpSendRequestDto request) {
        String identifier = resolveIdentifier(request);
        otpService.sendOtp(identifier);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @Operation(summary = "Resend OTP", description = "Resends the OTP. Subject to resend cooldown policy.")
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody OtpSendRequestDto request) {
        String identifier = resolveIdentifier(request);
        otpService.resendOtp(identifier);
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully"));
    }

    private String resolveIdentifier(OtpSendRequestDto request) {
        if (StringUtils.hasText(request.getEmail())) return request.getEmail();
        if (StringUtils.hasText(request.getPhone())) return request.getPhone();
        throw new InvalidCredentialsException();
    }
}
