package com.expenseflow.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

    private HashUtil() {}

    /**
     * Computes the SHA-256 hash of a plain text string and returns the hex format.
     */
    public static String sha256(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hash algorithm is not available in current JVM", e);
        }
    }
}
