package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Used to obtain a new access token using a valid refresh token.
 */
@Data
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
