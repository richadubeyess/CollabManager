
package com.richa.service;

import com.richa.model.PasswordResetToken;
import com.richa.repository.PasswordResetTokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceImplementationTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private PasswordResetTokenServiceImplementation tokenService;

    private PasswordResetToken resetToken;

    @BeforeEach
    void setUp() {
        resetToken = new PasswordResetToken();
    }

    @Test
    void findByToken_whenTokenExists_returnsToken() {

        String token = "test-token";

        when(passwordResetTokenRepository.findByToken(token))
                .thenReturn(resetToken);

        PasswordResetToken result = tokenService.findByToken(token);

        assertNotNull(result);
        assertSame(resetToken, result);

        verify(passwordResetTokenRepository).findByToken(token);
    }

    @Test
    void findByToken_whenTokenDoesNotExist_returnsNull() {

        String token = "invalid-token";

        when(passwordResetTokenRepository.findByToken(token))
                .thenReturn(null);

        PasswordResetToken result = tokenService.findByToken(token);

        assertNull(result);

        verify(passwordResetTokenRepository).findByToken(token);
    }

    @Test
    void delete_whenTokenIsProvided_deletesToken() {

        tokenService.delete(resetToken);

        verify(passwordResetTokenRepository).delete(resetToken);
    }
}
