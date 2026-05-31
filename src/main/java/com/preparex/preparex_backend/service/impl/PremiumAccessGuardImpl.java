package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.exception.PremiumRequiredException;
import com.preparex.preparex_backend.service.PremiumAccessGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default implementation of PremiumAccessGuard.
 *
 * <p><strong>Phase 1 stub:</strong> Since the User entity does not yet have a
 * subscriptionTier field, all users are treated as FREE tier.
 * This will be updated when the premium subscription system is built.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumAccessGuardImpl implements PremiumAccessGuard {

    /**
     * Checks access and throws PremiumRequiredException for free users
     * attempting to access premium resources.
     */
    @Override
    public void checkAccess(UUID userId, boolean resourceIsPremium) {
        if (!resourceIsPremium) {
            return;
        }

        if (!isPremiumUser(userId)) {
            log.warn("Free user {} attempted to access premium content", userId);
            throw new PremiumRequiredException();
        }
    }

    /**
     * Phase 1 stub: always returns false (all users are free tier).
     * Will be updated to check user's subscription tier from Redis/DB.
     */
    @Override
    public boolean isPremiumUser(UUID userId) {
        // TODO: Phase N — read user subscription tier from Redis cache or DB
        return false;
    }
}
