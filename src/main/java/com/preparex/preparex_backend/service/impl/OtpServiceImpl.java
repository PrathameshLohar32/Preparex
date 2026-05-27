package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.config.AppAuthProperties;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.exception.InvalidOtpException;
import com.preparex.preparex_backend.exception.OtpExpiredException;
import com.preparex.preparex_backend.exception.OtpRateLimitExceededException;
import com.preparex.preparex_backend.redis.model.OtpData;
import com.preparex.preparex_backend.service.OtpNotificationService;
import com.preparex.preparex_backend.service.OtpService;
import com.preparex.preparex_backend.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * OTP lifecycle management.
 *
 * Rate limiting:
 * - Max retries (default 5) before locking out verification.
 * - Resend cooldown (default 60s) enforced via a separate Redis key.
 *
 * Security:
 * - Raw OTP is NEVER stored — only its BCrypt hash.
 * - Raw OTP is NEVER logged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AppAuthProperties authProperties;
    private final OtpNotificationService otpNotificationService;

    @Override
    public void sendOtp(String identifier) {
        checkResendCooldown(identifier);
        generateAndStoreOtp(identifier);
    }

    @Override
    public void resendOtp(String identifier) {
        checkResendCooldown(identifier);
        generateAndStoreOtp(identifier);
    }

    @Override
    public void verifyOtp(String identifier, String rawOtp) {
        OtpData otpData = getOtpDataOrThrow(identifier);
        enforceRetryLimit(identifier, otpData);

        if (!OtpUtil.verifyOtp(rawOtp, otpData.getOtpHash())) {
            otpData.setRetryCount(otpData.getRetryCount() + 1);
            updateOtpData(identifier, otpData);
            throw new InvalidOtpException();
        }

        redisTemplate.delete(RedisKeyConstants.otpKey(identifier));
        log.info("OTP verified successfully for identifier={}", maskIdentifier(identifier));
    }

    private void generateAndStoreOtp(String identifier) {
        String rawOtp = OtpUtil.generateOtp();
        String otpHash = OtpUtil.hashOtp(rawOtp);

        OtpData otpData = OtpData.builder()
                .identifier(identifier)
                .otpHash(otpHash)
                .createdAt(Instant.now())
                .retryCount(0)
                .resendCount(0)
                .build();

        long ttl = authProperties.getOtp().getExpiryMinutes();
        redisTemplate.opsForValue().set(
                RedisKeyConstants.otpKey(identifier), otpData, ttl, TimeUnit.MINUTES);

        long cooldown = authProperties.getOtp().getResendCooldownSeconds();
        redisTemplate.opsForValue().set(
                RedisKeyConstants.otpResendKey(identifier), "1", cooldown, TimeUnit.SECONDS);

        otpNotificationService.sendOtp(identifier, rawOtp);
        log.info("OTP sent for identifier={}", maskIdentifier(identifier));
    }

    private void checkResendCooldown(String identifier) {
        Boolean cooldownActive = redisTemplate.hasKey(RedisKeyConstants.otpResendKey(identifier));
        if (Boolean.TRUE.equals(cooldownActive)) {
            throw new OtpRateLimitExceededException("resend");
        }
    }

    private OtpData getOtpDataOrThrow(String identifier) {
        Object data = redisTemplate.opsForValue().get(RedisKeyConstants.otpKey(identifier));
        if (!(data instanceof OtpData otpData)) {
            throw new OtpExpiredException();
        }
        return otpData;
    }

    private void enforceRetryLimit(String identifier, OtpData otpData) {
        if (otpData.getRetryCount() >= authProperties.getOtp().getMaxRetries()) {
            redisTemplate.delete(RedisKeyConstants.otpKey(identifier));
            throw new OtpRateLimitExceededException("verify");
        }
    }

    private void updateOtpData(String identifier, OtpData otpData) {
        Long ttl = redisTemplate.getExpire(RedisKeyConstants.otpKey(identifier), TimeUnit.SECONDS);
        long remainingTtl = (ttl != null && ttl > 0) ? ttl : authProperties.getOtp().getExpiryMinutes() * 60L;
        redisTemplate.opsForValue().set(
                RedisKeyConstants.otpKey(identifier), otpData, remainingTtl, TimeUnit.SECONDS);
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "***";
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }
}
