package com.preparex.preparex_backend.constant;

/**
 * Security-related constants used across filters, services, and utilities.
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /** Claim keys inside the JWT access token payload */
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_SESSION_ID = "sessionId";

    /** Default role assigned to all users */
    public static final String ROLE_USER = "ROLE_USER";
}
