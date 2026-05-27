package com.preparex.preparex_backend.service.strategy;

import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.enums.AuthType;

/**
 * Strategy interface for all authentication methods.
 * Each implementation handles a specific AuthType and returns an AuthenticatedUserContext on success.
 *
 * New auth providers (GitHub, Facebook, Magic Link, etc.) should implement this interface
 * and register as a Spring bean — the factory will auto-detect them.
 */
public interface AuthStrategy {

    /**
     * Returns the AuthType this strategy handles.
     */
    AuthType getSupportedAuthType();

    /**
     * Authenticates the user based on the provided request.
     *
     * @param request the unified login request
     * @return authenticated user context on success
     * @throws com.preparex.preparex_backend.exception.BaseException on authentication failure
     */
    AuthenticatedUserContext authenticate(LoginRequestDto request);
}
