package com.richa.model;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void isExpired_whenExpiryDateIsInPast_returnsTrue() {

        PasswordResetToken token = new PasswordResetToken(
                "test-token",
                new User(),
                new Date(System.currentTimeMillis() - 60000)
        );

        assertTrue(token.isExpired());
    }

    @Test
    void isExpired_whenExpiryDateIsInFuture_returnsFalse() {

        PasswordResetToken token = new PasswordResetToken(
                "test-token",
                new User(),
                new Date(System.currentTimeMillis() + 60000)
        );

        assertFalse(token.isExpired());
    }
}