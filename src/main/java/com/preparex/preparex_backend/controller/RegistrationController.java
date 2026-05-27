package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.GoogleCompleteRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterInitiateRequestDto;
import com.preparex.preparex_backend.dto.request.RegisterVerifyRequestDto;
import com.preparex.preparex_backend.dto.response.AuthResponseDto;
import com.preparex.preparex_backend.service.RegistrationService;
import com.preparex.preparex_backend.util.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Registration endpoints: initiate (OTP), verify (OTP), and Google SSO completion.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/register")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "Two-step OTP registration and Google SSO registration APIs")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(
            summary = "Initiate registration",
            description = "Validates uniqueness, hashes password, stores temp data, and sends OTP"
    )
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<Void>> initiateRegistration(
            @Valid @RequestBody RegisterInitiateRequestDto request) {

        registrationService.initiateRegistration(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("OTP sent. Please verify to complete registration"));
    }

    @Operation(
            summary = "Verify OTP and complete registration",
            description = "Verifies OTP, creates user account and returns auth tokens"
    )
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyRegistration(
            @Valid @RequestBody RegisterVerifyRequestDto request,
            HttpServletRequest httpRequest) {

        String ipAddress = RequestUtil.extractIpAddress(httpRequest);
        String userAgent = RequestUtil.extractUserAgent(httpRequest);

        AuthResponseDto authResponse = registrationService.verifyRegistration(request, ipAddress, userAgent);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }

    @Operation(
            summary = "Complete Google SSO registration",
            description = "Creates account for new Google users who need to choose a username"
    )
    @PostMapping("/google/complete")
    public ResponseEntity<ApiResponse<AuthResponseDto>> completeGoogleRegistration(
            @Valid @RequestBody GoogleCompleteRequestDto request,
            HttpServletRequest httpRequest) {

        String ipAddress = RequestUtil.extractIpAddress(httpRequest);
        String userAgent = RequestUtil.extractUserAgent(httpRequest);

        AuthResponseDto authResponse = registrationService.completeGoogleRegistration(request, ipAddress, userAgent);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Google registration completed", authResponse));
    }
}
