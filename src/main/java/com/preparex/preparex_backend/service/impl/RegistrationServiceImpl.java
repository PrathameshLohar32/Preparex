package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.config.AppAuthProperties;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.dto.request.GoogleCompleteRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterInitiateRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterVerifyRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.dto.response.GoogleVerifyResultDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.entity.UserIdentity;
import com.preparex.preparex_backend.enums.AuthProvider;
import com.preparex.preparex_backend.exception.DuplicateResourceException;
import com.preparex.preparex_backend.exception.RegistrationDataExpiredException;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import com.preparex.preparex_backend.redis.model.TempRegistrationData;
import com.preparex.preparex_backend.repository.UserIdentityRepository;
import com.preparex.preparex_backend.service.*;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import com.preparex.preparex_backend.util.HashUtil;
import com.preparex.preparex_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates the full registration flow (both OTP and Google SSO).
 *
 * OTP Registration Flow:
 * 1. Validate uniqueness → Hash password → Store TempRegistrationData in Redis → Send OTP
 * 2. Verify OTP → Create User + UserIdentity → Create session → Return tokens
 *
 * Google Registration Flow:
 * 1. Verify Google token → Validate username uniqueness → Create User + Google UserIdentity
 *    → Create session → Return tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final OtpService otpService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final UserIdentityRepository userIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AppAuthProperties authProperties;
    private final JwtUtil jwtUtil;

    @Override
    public void initiateRegistration(RegisterInitiateRequestDto request) {
        validateAtLeastOneIdentifier(request.getEmail(), request.getPhone());
        userService.validateUniqueness(request.getEmail(), request.getPhone(), request.getUsername());

        String passwordHash = passwordEncoder.encode(request.getPassword());
        String identifier = resolveIdentifier(request.getEmail(), request.getPhone());

        TempRegistrationData tempData = TempRegistrationData.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordHash)
                .build();

        long ttl = authProperties.getRegistration().getTempDataTtlMinutes();
        redisTemplate.opsForValue().set(
                RedisKeyConstants.tempRegistrationKey(identifier), tempData, ttl, TimeUnit.MINUTES);

        otpService.sendOtp(identifier);
        log.info("Registration initiated for identifier={}", maskIdentifier(identifier));
    }

    @Override
    @Transactional
    public AuthResponseDto verifyRegistration(RegisterVerifyRequestDto request,
                                               String ipAddress, String userAgent) {
        String identifier = resolveIdentifier(request.getEmail(), request.getPhone());

        TempRegistrationData tempData = getTempDataOrThrow(identifier);
        otpService.verifyOtp(identifier, request.getOtp());

        User user = userService.createUser(
                tempData.getName(), tempData.getUsername(),
                tempData.getEmail(), tempData.getPhone(), tempData.getPasswordHash());

        createLocalIdentity(user);

        redisTemplate.delete(RedisKeyConstants.tempRegistrationKey(identifier));

        return createSessionAndTokens(user, request.getDeviceInfo(), ipAddress, userAgent);
    }

    @Override
    @Transactional
    public AuthResponseDto completeGoogleRegistration(GoogleCompleteRequestDto request,
                                                       String ipAddress, String userAgent) {
        GoogleVerifyResultDto verifyResult =
                googleTokenVerificationService.verifyIdToken(request.getIdToken());

        userIdentityRepository.findByProviderAndProviderUserId(
                AuthProvider.GOOGLE, verifyResult.getGoogleUserId())
                .ifPresent(identity -> {
                    throw new DuplicateResourceException("Google account", verifyResult.getEmail());
                });

        if (userService.findByEmail(verifyResult.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email", verifyResult.getEmail());
        }

        if (StringUtils.hasText(request.getUsername())) {
            userService.validateUniqueness(null, null, request.getUsername());
        }

        User user = userService.createUser(
                verifyResult.getName(), request.getUsername(),
                verifyResult.getEmail(), null, null);

        UserIdentity googleIdentity = UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(verifyResult.getGoogleUserId())
                .build();
        userIdentityRepository.save(googleIdentity);

        log.info("Google registration completed for userId={}", user.getId());
        return createSessionAndTokens(user, request.getDeviceInfo(), ipAddress, userAgent);
    }

    private AuthResponseDto createSessionAndTokens(User user, String deviceInfo,
                                                    String ipAddress, String userAgent) {
        AuthenticatedUserContext context = AuthenticatedUserContext.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .roles(List.of(SecurityConstants.ROLE_USER))
                .build();

        String rawRefreshToken = jwtUtil.generateRefreshToken();

        ActiveSessionData session = sessionService.createSession(
                user.getId().toString(),
                HashUtil.hashToken(rawRefreshToken),
                deviceInfo, ipAddress, userAgent);

        AuthResponseDto authResponse = tokenService.generateTokenPair(context, session.getSessionId());
        return AuthResponseDto.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(rawRefreshToken)
                .sessionId(session.getSessionId())
                .accessTokenExpiresInSeconds(authResponse.getAccessTokenExpiresInSeconds())
                .user(authResponse.getUser())
                .build();
    }

    private void createLocalIdentity(User user) {
        UserIdentity identity = UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId(null)
                .build();
        userIdentityRepository.save(identity);
    }

    private TempRegistrationData getTempDataOrThrow(String identifier) {
        Object data = redisTemplate.opsForValue().get(RedisKeyConstants.tempRegistrationKey(identifier));
        if (!(data instanceof TempRegistrationData tempData)) {
            throw new RegistrationDataExpiredException();
        }
        return tempData;
    }

    private String resolveIdentifier(String email, String phone) {
        return StringUtils.hasText(email) ? email : phone;
    }

    private void validateAtLeastOneIdentifier(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new com.preparex.preparex_backend.exception.InvalidCredentialsException();
        }
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "***";
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }
}
