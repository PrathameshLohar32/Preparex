package com.preparex.preparex_backend.enums;

/**
 * Reason codes for session termination — stored in session_history for audit.
 */
public enum LogoutReason {
    USER_LOGOUT,
    LOGOUT_ALL,
    SESSION_LIMIT_EXCEEDED,
    ADMIN_FORCED,
    TOKEN_EXPIRED
}
