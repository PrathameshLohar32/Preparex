package com.preparex.preparex_backend.dto.request;

import lombok.Data;

/**
 * Request for single-session logout. If sessionId is null, the current session (from JWT) is used.
 */
@Data
public class LogoutRequestDto {

    private String sessionId;
}
