package com.preparex.preparex_backend.service;

import java.util.UUID;

/**
 * Guard component for premium content access control.
 * Checks whether a user has the required subscription tier
 * to access premium-gated resources.
 */
public interface PremiumAccessGuard {

    /**
     * Checks if the user can access a premium resource.
     * Throws PremiumRequiredException if access is denied.
     *
     * @param userId            the user attempting access
     * @param resourceIsPremium whether the target resource is premium-gated
     */
    void checkAccess(UUID userId, boolean resourceIsPremium);

    /**
     * Returns whether the user has a premium subscription.
     *
     * @param userId the user to check
     * @return true if user has premium access
     */
    boolean isPremiumUser(UUID userId);
}
