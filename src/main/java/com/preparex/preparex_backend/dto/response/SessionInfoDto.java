package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a single active session as returned by the GET /sessions endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDto {

    private String sessionId;
    private String deviceInfo;
    private String ipAddress;
    private Instant loggedInAt;
    private Instant lastAccessedAt;
    private Boolean current;
}
