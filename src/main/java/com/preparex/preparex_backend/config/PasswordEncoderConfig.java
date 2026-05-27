package com.preparex.preparex_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoding configuration. Uses Argon2id as recommended in the security standards.
 * Parameters are tuned for production: memory=65536 KB, iterations=2, parallelism=1.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // saltLength=16, hashLength=32, parallelism=1, memory=65536 KB, iterations=2
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
