package com.preparex.preparex_backend.service.strategy.impl;

import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.AuthType;
import com.preparex.preparex_backend.exception.InvalidCredentialsException;
import com.preparex.preparex_backend.exception.UserNotFoundException;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.OtpService;
import com.preparex.preparex_backend.service.strategy.AuthStrategy;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Handles phone + OTP authentication.
 *
 * Business rules:
 * - Looks up the user by phone number.
 * - Delegates OTP verification (including rate limiting) to OtpService.
 * - The OTP data is cleaned up from Redis after successful verification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpAuthStrategy implements AuthStrategy {

    private final UserRepository userRepository;
    private final OtpService otpService;

    @Override
    public AuthType getSupportedAuthType() {
        return AuthType.PHONE_OTP;
    }

    @Override
    public AuthenticatedUserContext authenticate(LoginRequestDto request) {
        String phone = request.getPhone();
        String otp = request.getOtp();

        if (!StringUtils.hasText(phone) || !StringUtils.hasText(otp)) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException(phone));

        otpService.verifyOtp(phone, otp);

        log.info("Phone OTP auth successful for userId={}", user.getId());

        return AuthenticatedUserContext.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .roles(List.of(SecurityConstants.ROLE_USER))
                .build();
    }
}
