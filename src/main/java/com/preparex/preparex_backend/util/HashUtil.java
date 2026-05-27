package com.preparex.preparex_backend.util;

import com.preparex.preparex_backend.exception.InvalidTokenException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility for SHA-256 hashing and verification of tokens (refresh tokens).
 * Only the hash is ever stored in Redis — never the raw token.
 */
public final class HashUtil {

    private static final String ALGORITHM = "SHA-256";

    private HashUtil() {}

    /**
     * Computes the SHA-256 hex hash of the given raw token.
     */
    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidTokenException("Token hashing algorithm unavailable");
        }
    }

    /**
     * Verifies a raw token against a stored hash using constant-time comparison.
     */
    public static boolean verifyToken(String rawToken, String storedHash) {
        if (rawToken == null || storedHash == null) {
            return false;
        }
        String computedHash = hashToken(rawToken);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
