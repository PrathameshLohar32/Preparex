package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Step 1 of registration flow. At least one of email or phone must be provided.
 * Validated further in RegistrationService.
 */
@Data
public class RegisterInitiateRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    private String email;
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String deviceInfo;
}
