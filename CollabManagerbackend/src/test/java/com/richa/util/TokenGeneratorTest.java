
package com.richa.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class TokenGeneratorTest {

    @Test
    void generateToken_returnsValidToken() {

        String email = "test@example.com";
        Long projectId = 101L;
        int length = 32;

        String token = TokenGenerator.generateToken(
                email, projectId, length
        );

        assertNotNull(token);

        String[] parts = token.split(":", 2);

        assertEquals(2, parts.length);
        assertEquals(email + ":" + projectId, parts[1]);
    }

    @Test
    void generateToken_generatesRandomBytesOfRequestedLength() {

        int length = 32;

        String token = TokenGenerator.generateToken(
                "test@example.com", 101L, length
        );

        String randomPart = token.split(":", 2)[0];

        byte[] decodedBytes = Base64.getUrlDecoder()
                .decode(randomPart);

        assertEquals(length, decodedBytes.length);
    }

    @Test
    void generateToken_generatesUniqueTokens() {

        String token1 = TokenGenerator.generateToken(
                "test@example.com", 101L, 32
        );

        String token2 = TokenGenerator.generateToken(
                "test@example.com", 101L, 32
        );

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_usesUrlSafeBase64WithoutPadding() {

        String token = TokenGenerator.generateToken(
                "test@example.com", 101L, 32
        );

        String randomPart = token.split(":", 2)[0];

        assertFalse(randomPart.contains("+"));
        assertFalse(randomPart.contains("/"));
        assertFalse(randomPart.contains("="));
    }

    @Test
    void generateToken_containsCorrectProjectId() {

        String token = TokenGenerator.generateToken(
                "user@gmail.com", 500L, 32
        );

        assertTrue(token.endsWith("user@gmail.com:500"));
    }
}
