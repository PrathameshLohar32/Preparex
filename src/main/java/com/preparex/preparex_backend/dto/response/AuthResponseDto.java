package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned after successful login, registration, or token refresh.
 * Contains both tokens and a minimal user summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    private String sessionId;
    private long accessTokenExpiresInSeconds;
    private UserSummaryDto user;
}
