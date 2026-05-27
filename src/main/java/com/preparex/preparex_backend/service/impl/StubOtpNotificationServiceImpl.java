package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.service.OtpNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of OtpNotificationService for development.
 * Logs the delivery event without exposing the OTP value.
 *
 * Replace this with a real provider implementation (Twilio, MSG91, AWS SNS, etc.)
 * for staging/production. Annotate the real impl with @Primary or use profiles.
 */
@Slf4j
@Service
public class StubOtpNotificationServiceImpl implements OtpNotificationService {

    @Async("notificationExecutor")
    @Override
    public void sendOtp(String identifier, String rawOtp) {
        // In dev: log identifier but never the OTP itself
        log.info("[STUB] OTP notification triggered for identifier={}. " +
                "Integrate a real SMS/email provider for production.", maskIdentifier(identifier));

        // TODO: Replace with actual provider call:
        // if (isPhone(identifier)) { smsProvider.send(identifier, rawOtp); }
        // else { emailProvider.sendOtpEmail(identifier, rawOtp); }
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "***";
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }
}
