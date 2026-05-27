package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Completes Google SSO registration for new users who need to choose a username.
 */
@Data
public class GoogleCompleteRequestDto {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    private String deviceInfo;
}
