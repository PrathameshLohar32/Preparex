package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.AuthType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Unified login request. Only the fields relevant to the authType need to be populated.
 *
 * PASSWORD:  email (or username) + password
 * PHONE_OTP: phone + otp
 * EMAIL_OTP: email + otp
 * GOOGLE:    idToken
 */
@Data
public class LoginRequestDto {

    @NotNull(message = "authType is required")
    private AuthType authType;

    private String email;
    private String username;
    private String phone;
    private String password;
    private String otp;
    private String idToken;
    private String deviceInfo;
}
