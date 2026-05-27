package com.preparex.preparex_backend.enums;

/**
 * Supported authentication types for the unified login API.
 * Add new values here when introducing a new auth provider.
 */
public enum AuthType {
    PASSWORD,
    PHONE_OTP,
    EMAIL_OTP,
    GOOGLE,
    GITHUB,
    FACEBOOK
}
