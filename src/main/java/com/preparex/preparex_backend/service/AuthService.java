package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.dto.request.RefreshTokenRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;

public interface AuthService {

    /**
     * Unified login — resolves auth strategy by authType and executes the full auth flow.
     */
    AuthResponseDto login(LoginRequestDto request, String ipAddress, String userAgent);

    /**
     * Rotates the refresh token and issues a new token pair.
     */
    AuthResponseDto refresh(RefreshTokenRequestDto request, String currentSessionId);

    /**
     * Logs out a specific session (defaults to current session if sessionId is null in request).
     */
    void logout(String sessionId, String userId);

    /**
     * Logs out all active sessions for the user.
     */
    void logoutAll(String userId);
}
