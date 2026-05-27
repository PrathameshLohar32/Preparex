package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.service.strategy.AuthenticatedUserContext;

public interface TokenService {

    /**
     * Generates an access token and a new refresh token for the given context and session.
     * Stores only the refresh token hash in the session.
     */
    AuthResponseDto generateTokenPair(AuthenticatedUserContext context, String sessionId);

    /**
     * Validates the refresh token against the session's stored hash and rotates it.
     * Returns a new token pair. Invalidates the old refresh token hash.
     *
     * @throws com.preparex.preparex_backend.exception.InvalidTokenException on mismatch
     * @throws com.preparex.preparex_backend.exception.SessionExpiredException if session not found
     */
    AuthResponseDto rotateRefreshToken(String rawRefreshToken, String sessionId);
}
