package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.response.GoogleVerifyResultDto;

/**
 * Verifies a Google ID token against Google's public keys.
 */
public interface GoogleTokenVerificationService {

    /**
     * Verifies the given ID token and extracts user claims.
     *
     * @throws com.preparex.preparex_backend.exception.GoogleAuthException on token failure
     */
    GoogleVerifyResultDto verifyIdToken(String idToken);
}
