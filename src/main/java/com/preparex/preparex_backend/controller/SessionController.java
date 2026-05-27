package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.response.SessionInfoDto;
import com.preparex.preparex_backend.mapper.SessionMapper;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Session management endpoints for listing and revoking active sessions.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Active session listing and revocation APIs")
public class SessionController {

    private final SessionService sessionService;
    private final SessionMapper sessionMapper;

    @Operation(summary = "List all active sessions", description = "Returns all active sessions for the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionInfoDto>>> getActiveSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String currentSessionId = userDetails.getSessionId();
        String userId = userDetails.getUserId().toString();

        List<SessionInfoDto> sessions = sessionService.getActiveSessions(userId).stream()
                .map(session -> {
                    SessionInfoDto dto = sessionMapper.toSessionInfoDto(session);
                    dto.setCurrent(session.getSessionId().equals(currentSessionId));
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved", sessions));
    }

    @Operation(summary = "Revoke a specific session", description = "Terminates the specified session by ID")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUserId().toString();
        sessionService.invalidateSession(sessionId, userId);

        log.info("Session revoked sessionId={} by userId={}", sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Session revoked successfully"));
    }
}
