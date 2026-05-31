package com.preparex.preparex_backend.exception;

/**
 * Thrown when a free-tier user attempts to access premium-gated content.
 * Maps to HTTP 403 Forbidden.
 */
public class PremiumRequiredException extends BaseException {

    public PremiumRequiredException(String message) {
        super(message, "PREMIUM_REQUIRED");
    }

    public PremiumRequiredException() {
        super("Premium subscription required to access this content", "PREMIUM_REQUIRED");
    }
}
