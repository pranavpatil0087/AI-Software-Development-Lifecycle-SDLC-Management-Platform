package com.sdlcplatform.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically-random, URL-safe tokens for email verification
 * and password reset links. Not JWTs — these are simple opaque tokens
 * looked up directly in the database.
 */
public final class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenGenerator() {
    }

    public static String generate() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
