
package com.richa.controller;

import com.richa.exception.UserException;
import com.richa.model.PasswordResetToken;
import com.richa.model.User;
import com.richa.request.ResetPasswordRequest;
import com.richa.response.ApiResponse;
import com.richa.service.PasswordResetTokenService;
import com.richa.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordControllerTest {

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ResetPasswordController resetPasswordController;

    @Mock
    private ResetPasswordRequest request;

    @Mock
    private PasswordResetToken resetToken;

    @Mock
    private User user;

    @Test
    void resetPassword_whenTokenIsValid_returnsSuccess()
            throws Exception {

        when(request.getToken()).thenReturn("valid-token");
        when(request.getPassword()).thenReturn("newPassword123");

        when(passwordResetTokenService.findByToken("valid-token"))
                .thenReturn(resetToken);

        when(resetToken.isExpired()).thenReturn(false);
        when(resetToken.getUser()).thenReturn(user);

        ResponseEntity<ApiResponse> response =
                resetPasswordController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password updated successfully.",
                response.getBody().getMessage());

        verify(userService).updatePassword(user, "newPassword123");
        verify(passwordResetTokenService).delete(resetToken);
    }

    @Test
    void resetPassword_whenTokenDoesNotExist_throwsException() {

        when(request.getToken()).thenReturn("invalid-token");

        when(passwordResetTokenService.findByToken("invalid-token"))
                .thenReturn(null);

        assertThrows(UserException.class,
                () -> resetPasswordController.resetPassword(request));

        verify(userService, never())
                .updatePassword(any(), anyString());

        verify(passwordResetTokenService, never())
                .delete(any());
    }

    @Test
    void resetPassword_whenTokenIsExpired_deletesTokenAndThrowsException() {

        when(request.getToken()).thenReturn("expired-token");

        when(passwordResetTokenService.findByToken("expired-token"))
                .thenReturn(resetToken);

        when(resetToken.isExpired()).thenReturn(true);

        assertThrows(UserException.class,
                () -> resetPasswordController.resetPassword(request));

        verify(passwordResetTokenService).delete(resetToken);

        verify(userService, never())
                .updatePassword(any(), anyString());
    }

    @Test
    void resetPassword1_whenUserExists_sendsResetEmail()
            throws Exception {

        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(user);

        ResponseEntity<ApiResponse> response =
                resetPasswordController.resetPassword1("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password reset email sent successfully.",
                response.getBody().getMessage());

        verify(userService).sendPasswordResetEmail(user);
    }

    @Test
    void resetPassword1_whenUserDoesNotExist_throwsException()
            throws Exception {

        when(userService.findUserByEmail("unknown@example.com"))
                .thenReturn(null);

        assertThrows(UserException.class,
                () -> resetPasswordController.resetPassword1(
                        "unknown@example.com"));

        verify(userService, never()).sendPasswordResetEmail(any());
    }
}
