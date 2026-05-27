package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Step 2 of registration. The identifier (email or phone) is used to
 * look up the temporary registration data in Redis.
 */
@Data
public class RegisterVerifyRequestDto {

    private String email;
    private String phone;

    @NotBlank(message = "OTP is required")
    private String otp;

    private String deviceInfo;
}
