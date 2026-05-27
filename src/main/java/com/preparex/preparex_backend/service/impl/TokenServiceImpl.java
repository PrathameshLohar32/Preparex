package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.dto.response.UserSummaryDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.exception.InvalidTokenException;
import com.preparex.preparex_backend.exception.SessionExpiredException;
import com.preparex.preparex_backend.mapper.UserMapper;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import com.preparex.preparex_backend.service.SessionService;
import com.preparex.preparex_backend.service.TokenService;
import com.preparex.preparex_backend.service.UserService;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;
import com.preparex.preparex_backend.util.HashUtil;
import com.preparex.preparex_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Token generation and refresh rotation.
 *
 * Security:
 * - Only refresh token hashes are stored in Redis.
 * - On rotation, the old hash is replaced atomically with the new one.
 * - Mismatched refresh tokens trigger InvalidTokenException (possible replay attack).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public AuthResponseDto generateTokenPair(AuthenticatedUserContext context, String sessionId) {
        String accessToken = jwtUtil.generateAccessToken(
                context.getUserId(), context.getEmail(), context.getRoles(), sessionId);
        String refreshToken = jwtUtil.generateRefreshToken();

        User user = userService.findByIdOrThrow(UUID.fromString(context.getUserId()));
        UserSummaryDto userSummary = userMapper.toUserSummaryDto(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .accessTokenExpiresInSeconds(jwtUtil.getAccessTokenExpirySeconds())
                .user(userSummary)
                .build();
    }

    @Override
    public AuthResponseDto rotateRefreshToken(String rawRefreshToken, String sessionId) {
        ActiveSessionData session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new SessionExpiredException();
        }

        if (!HashUtil.verifyToken(rawRefreshToken, session.getRefreshTokenHash())) {
            log.warn("Refresh token mismatch for sessionId={}. Possible replay attack.", sessionId);
            throw new InvalidTokenException("Refresh token is invalid or has already been used");
        }

        String newRefreshToken = jwtUtil.generateRefreshToken();
        session.setRefreshTokenHash(HashUtil.hashToken(newRefreshToken));
        sessionService.touchSession(sessionId);

        User user = userService.findByIdOrThrow(UUID.fromString(session.getUserId()));
        UserSummaryDto userSummary = userMapper.toUserSummaryDto(user);

        String newAccessToken = jwtUtil.generateAccessToken(
                session.getUserId(), user.getEmail(),
                java.util.List.of("ROLE_USER"), sessionId);

        log.info("Refresh token rotated for sessionId={}", sessionId);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .sessionId(sessionId)
                .accessTokenExpiresInSeconds(jwtUtil.getAccessTokenExpirySeconds())
                .user(userSummary)
                .build();
    }
}
