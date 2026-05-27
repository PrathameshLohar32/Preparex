package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.GoogleCompleteRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterInitiateRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterVerifyRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;

public interface RegistrationService {

    /**
     * Step 1: Validates uniqueness, hashes password, stores temp data in Redis, sends OTP.
     */
    void initiateRegistration(RegisterInitiateRequestDto request);

    /**
     * Step 2: Verifies OTP, creates User + UserIdentity, creates session, returns auth tokens.
     */
    AuthResponseDto verifyRegistration(RegisterVerifyRequestDto request, String ipAddress, String userAgent);

    /**
     * Completes Google SSO registration for new users by creating User + Google UserIdentity + session.
     */
    AuthResponseDto completeGoogleRegistration(GoogleCompleteRequestDto request, String ipAddress, String userAgent);
}
