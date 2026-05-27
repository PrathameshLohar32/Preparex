package com.preparex.preparex_backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.preparex.preparex_backend.config.AppAuthProperties;
import com.preparex.preparex_backend.dto.response.GoogleVerifyResultDto;
import com.preparex.preparex_backend.exception.GoogleAuthException;
import com.preparex.preparex_backend.service.GoogleTokenVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Verifies Google ID tokens using the official Google API client library.
 * Validates audience (client ID) and token signature automatically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTokenVerificationServiceImpl implements GoogleTokenVerificationService {

    private final AppAuthProperties authProperties;

    @Override
    public GoogleVerifyResultDto verifyIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(authProperties.getGoogle().getClientId()))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new GoogleAuthException("ID token verification failed — token may be invalid or expired");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String googleUserId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            log.info("Google ID token verified for email={}", email);

            return GoogleVerifyResultDto.builder()
                    .googleUserId(googleUserId)
                    .email(email)
                    .name(name)
                    .build();

        } catch (GoogleAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Google ID token", e);
            throw new GoogleAuthException("Unable to verify Google token: " + e.getMessage());
        }
    }
}
