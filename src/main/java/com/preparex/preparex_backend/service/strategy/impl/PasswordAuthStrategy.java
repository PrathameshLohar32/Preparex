package com.preparex.preparex_backend.service.strategy.impl;

import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.AuthType;
import com.preparex.preparex_backend.exception.InvalidCredentialsException;
import com.preparex.preparex_backend.exception.UserNotFoundException;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.strategy.AuthStrategy;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Handles email/username + password authentication.
 *
 * Business rules:
 * - Accepts either email or username as identifier.
 * - Password is verified against the Argon2 hash stored in the DB.
 * - Does NOT distinguish between "user not found" and "wrong password" in the public error message
 *   to prevent user enumeration attacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordAuthStrategy implements AuthStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthType getSupportedAuthType() {
        return AuthType.PASSWORD;
    }

    @Override
    public AuthenticatedUserContext authenticate(LoginRequestDto request) {
        if (!StringUtils.hasText(request.getPassword())) {
            throw new InvalidCredentialsException();
        }

        User user = resolveUser(request);
        verifyPassword(request.getPassword(), user);

        log.info("Password auth successful for userId={}", user.getId());

        return AuthenticatedUserContext.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .roles(List.of(SecurityConstants.ROLE_USER))
                .build();
    }

    private User resolveUser(LoginRequestDto request) {
        if (StringUtils.hasText(request.getEmail())) {
            return userRepository.findByEmail(request.getEmail())
                    .orElseThrow(InvalidCredentialsException::new);
        }
        if (StringUtils.hasText(request.getUsername())) {
            return userRepository.findByUsername(request.getUsername())
                    .orElseThrow(InvalidCredentialsException::new);
        }
        throw new InvalidCredentialsException();
    }

    private void verifyPassword(String rawPassword, User user) {
        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
    }
}
