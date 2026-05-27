package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.LoginRequestDto;
import com.preparex.preparex_backend.dto.request.LogoutRequestDto;
import com.preparex.preparex_backend.dto.request.RefreshTokenRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.AuthService;
import com.preparex.preparex_backend.util.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints: login, refresh, logout, logout-all.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, token refresh, and logout APIs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Unified login", description = "Supports PASSWORD, PHONE_OTP, EMAIL_OTP, and GOOGLE auth types")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {

        String ipAddress = RequestUtil.extractIpAddress(httpRequest);
        String userAgent = RequestUtil.extractUserAgent(httpRequest);

        AuthResponseDto authResponse = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @Operation(summary = "Refresh access token", description = "Rotates refresh token and issues a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String sessionId = userDetails != null ? userDetails.getSessionId() : null;
        AuthResponseDto authResponse = authService.refresh(request, sessionId);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @Operation(summary = "Logout current or specific session")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) LogoutRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String sessionId = (request != null && StringUtils.hasText(request.getSessionId()))
                ? request.getSessionId()
                : userDetails.getSessionId();

        authService.logout(sessionId, userDetails.getUserId().toString());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @Operation(summary = "Logout all active sessions")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        authService.logoutAll(userDetails.getUserId().toString());
        return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully"));
    }
}
