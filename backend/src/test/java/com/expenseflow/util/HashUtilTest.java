package com.expenseflow.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilTest {

    @Test
    void testSha256_ValidString_ReturnsExpectedHash() {
        String input = "test-token";
        // Expected SHA-256 for "test-token" in hex format
        String expectedHash = "4c5dc9b7708905f77f5e5d16316b5dfb425e68cb326dcd55a860e90a7707031e";

        String actualHash = HashUtil.sha256(input);

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test
    void testSha256_NullInput_ReturnsNull() {
        assertThat(HashUtil.sha256(null)).isNull();
    }

    @Test
    void testSha256_EmptyInput_ReturnsExpectedHash() {
        String input = "";
        // Expected SHA-256 for empty string
        String expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        assertThat(HashUtil.sha256(input)).isEqualTo(expectedHash);
    }
}
