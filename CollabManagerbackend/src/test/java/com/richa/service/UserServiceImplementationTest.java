
package com.richa.service;

import com.richa.exception.UserException;
import com.richa.model.PasswordResetToken;
import com.richa.model.User;
import com.richa.repository.PasswordResetTokenRepository;
import com.richa.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private UserServiceImplementation userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldPassword");
        user.setProjectSize(2);
    }

    // 1. Find user by email - success
    @Test
    void findUserByEmail_whenUserExists_returnsUser() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(user);

        User result =
                userService.findUserByEmail("test@example.com");

        assertNotNull(result);
        assertSame(user, result);

        verify(userRepository).findByEmail("test@example.com");
    }

    // 2. Find user by email - user doesn't exist
    @Test
    void findUserByEmail_whenUserDoesNotExist_throwsException() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(null);

        assertThrows(
                UserException.class,
                () -> userService.findUserByEmail("missing@example.com")
        );
    }

    // 3. Find user by ID - success
    @Test
    void findUserById_whenUserExists_returnsUser() throws Exception {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertSame(user, result);
        verify(userRepository).findById(1L);
    }

    // 4. Find user by ID - user doesn't exist
    @Test
    void findUserById_whenUserDoesNotExist_throwsException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserException.class,
                () -> userService.findUserById(99L)
        );
    }

    // 5. Update project size - normal case
    @Test
    void updateUsersProjectSize_whenValid_savesUpdatedUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUsersProjectSize(user, 3);

        assertEquals(5, user.getProjectSize());
        assertSame(user, result);

        verify(userRepository).save(user);
    }

    // 6. Update project size - sentinel value -1
    @Test
    void updateUsersProjectSize_whenResultIsMinusOne_doesNotSave() {
        user.setProjectSize(0);

        User result = userService.updateUsersProjectSize(user, -1);

        assertEquals(-1, user.getProjectSize());
        assertSame(user, result);

        verify(userRepository, never()).save(any(User.class));
    }

    // 7. Update password - password is encoded and saved
    @Test
    void updatePassword_encodesPasswordAndSavesUser() {
        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedPassword");

        userService.updatePassword(user, "newPassword");

        assertEquals("encodedPassword", user.getPassword());

        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    // 8. Send password reset email - token is saved and email sent
    @Test
    void sendPasswordResetEmail_savesTokenAndSendsEmail() {
        userService.sendPasswordResetEmail(user);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(passwordResetTokenRepository)
                .save(tokenCaptor.capture());

        assertNotNull(tokenCaptor.getValue());

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(javaMailSender).send(mailCaptor.capture());

        SimpleMailMessage email = mailCaptor.getValue();

        assertArrayEquals(
                new String[]{"test@example.com"},
                email.getTo()
        );
        assertEquals("Password Reset", email.getSubject());

        assertNotNull(email.getText());
        assertTrue(email.getText().contains("token="));
    }

    // 9. Send password reset email - mail provider fails
    @Test
    void sendPasswordResetEmail_whenMailFails_propagatesException() {
        doThrow(new RuntimeException("Mail server failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(
                RuntimeException.class,
                () -> userService.sendPasswordResetEmail(user)
        );

        verify(passwordResetTokenRepository)
                .save(any(PasswordResetToken.class));
    }
}
