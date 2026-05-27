package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO holding the result of Google ID token verification.
 * Not exposed to clients directly — used internally between GoogleAuthStrategy and RegistrationService.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleVerifyResultDto {

    private String googleUserId;
    private String email;
    private String name;
    private boolean isNewUser;
}
