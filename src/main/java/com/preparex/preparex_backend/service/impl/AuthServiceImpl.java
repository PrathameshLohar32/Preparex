package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.dto.request.RefreshTokenRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import com.preparex.preparex_backend.service.AuthService;
import com.preparex.preparex_backend.service.SessionService;
import com.preparex.preparex_backend.service.TokenService;
import com.preparex.preparex_backend.service.factory.AuthStrategyFactory;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import com.preparex.preparex_backend.service.strategy.AuthStrategy;
import com.preparex.preparex_backend.util.HashUtil;
import com.preparex.preparex_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Core authentication orchestrator.
 *
 * Login flow:
 * 1. Resolve auth strategy via AuthStrategyFactory
 * 2. Authenticate user → get AuthenticatedUserContext
 * 3. Create session in Redis
 * 4. Generate refresh token (store hash only)
 * 5. Generate JWT access token
 * 6. Return AuthResponseDto
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthStrategyFactory authStrategyFactory;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDto login(LoginRequestDto request, String ipAddress, String userAgent) {
        log.info("Login attempt authType={}", request.getAuthType());

        AuthStrategy strategy = authStrategyFactory.resolve(request.getAuthType());
        AuthenticatedUserContext context = strategy.authenticate(request);

        String rawRefreshToken = jwtUtil.generateRefreshToken();

        ActiveSessionData session = sessionService.createSession(
                context.getUserId(),
                HashUtil.hashToken(rawRefreshToken),
                request.getDeviceInfo(),
                ipAddress,
                userAgent);

        AuthResponseDto authResponse = tokenService.generateTokenPair(context, session.getSessionId());

        log.info("Login successful userId={} authType={} sessionId={}",
                context.getUserId(), request.getAuthType(), session.getSessionId());

        return AuthResponseDto.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(rawRefreshToken)
                .sessionId(session.getSessionId())
                .accessTokenExpiresInSeconds(authResponse.getAccessTokenExpiresInSeconds())
                .user(authResponse.getUser())
                .build();
    }

    @Override
    public AuthResponseDto refresh(RefreshTokenRequestDto request, String currentSessionId) {
        log.info("Token refresh requested for sessionId={}", currentSessionId);
        return tokenService.rotateRefreshToken(request.getRefreshToken(), currentSessionId);
    }

    @Override
    public void logout(String sessionId, String userId) {
        sessionService.invalidateSession(sessionId, userId);
        log.info("Logged out sessionId={} userId={}", sessionId, userId);
    }

    @Override
    public void logoutAll(String userId) {
        sessionService.invalidateAllSessions(userId);
        log.info("All sessions logged out for userId={}", userId);
    }
}
