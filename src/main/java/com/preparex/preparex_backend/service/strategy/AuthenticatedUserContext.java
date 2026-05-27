package com.preparex.preparex_backend.service.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Value object returned by each AuthStrategy after successful authentication.
 * Carries the resolved user context needed for session creation and token generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserContext {

    private String userId;
    private String email;
    private String phone;
    private String name;
    private List<String> roles;
}
