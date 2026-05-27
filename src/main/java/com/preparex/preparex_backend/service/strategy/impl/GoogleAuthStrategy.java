package com.preparex.preparex_backend.service.strategy.impl;

import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.dto.response.GoogleVerifyResultDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.entity.UserIdentity;
import com.preparex.preparex_backend.enums.AuthProvider;
import com.preparex.preparex_backend.enums.AuthType;
import com.preparex.preparex_backend.exception.GoogleAuthException;
import com.preparex.preparex_backend.repository.UserIdentityRepository;
import com.preparex.preparex_backend.service.GoogleTokenVerificationService;
import com.preparex.preparex_backend.service.strategy.AuthStrategy;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Handles Google SSO authentication for existing users.
 *
 * Business rules:
 * - Verifies the Google ID token with Google's public keys.
 * - Looks up the UserIdentity by provider + Google userId (sub claim).
 * - If the identity exists, the user is logged in.
 * - If no identity is found, throws GoogleAuthException with a signal for the frontend
 *   to redirect to the /register/google/complete endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthStrategy implements AuthStrategy {

    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    public AuthType getSupportedAuthType() {
        return AuthType.GOOGLE;
    }

    @Override
    public AuthenticatedUserContext authenticate(LoginRequestDto request) {
        if (!StringUtils.hasText(request.getIdToken())) {
            throw new GoogleAuthException("ID token is required");
        }

        GoogleVerifyResultDto verifyResult =
                googleTokenVerificationService.verifyIdToken(request.getIdToken());

        UserIdentity identity = userIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, verifyResult.getGoogleUserId())
                .orElseThrow(() -> new GoogleAuthException("NEW_USER:" + verifyResult.getEmail()));

        User user = identity.getUser();

        log.info("Google auth successful for userId={}", user.getId());

        return AuthenticatedUserContext.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .roles(List.of(SecurityConstants.ROLE_USER))
                .build();
    }
}
