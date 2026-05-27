package com.preparex.preparex_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed configuration properties bound from application.yml under the "app.auth" prefix.
 * All auth-related configuration values flow through this class to avoid scattered @Value usages.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AppAuthProperties {

    private Jwt jwt = new Jwt();
    private Session session = new Session();
    private Otp otp = new Otp();
    private Registration registration = new Registration();
    private Google google = new Google();

    @Data
    public static class Jwt {
        private String secret;
        private int accessTokenExpiryMinutes = 15;
        private int refreshTokenExpiryDays = 7;
    }

    @Data
    public static class Session {
        /** Maximum number of concurrent active sessions per user. Configurable via MAX_ACTIVE_SESSIONS env var. */
        private int maxActiveSessions = 2;
        private int ttlDays = 7;
    }

    @Data
    public static class Otp {
        private int expiryMinutes = 5;
        private int maxRetries = 5;
        private int resendCooldownSeconds = 60;
    }

    @Data
    public static class Registration {
        private int tempDataTtlMinutes = 10;
    }

    @Data
    public static class Google {
        private String clientId;
    }
}
