package com.preparex.preparex_backend.dto.request;

import lombok.Data;

/**
 * Request to send or resend an OTP to a given email or phone.
 * At least one of email or phone must be provided.
 */
@Data
public class OtpSendRequestDto {

    private String email;
    private String phone;
}
