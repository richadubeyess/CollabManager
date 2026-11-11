
package com.richa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richa.model.PasswordResetToken;
import com.richa.model.User;
import com.richa.repository.ChatRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.repository.PasswordResetTokenRepository;
import com.richa.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:collabtest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "app.frontend.url=http://localhost:5173"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JavaMailSender javaMailSender;

    private User testUser;

    @BeforeEach
    void setUp() {

        // Clean dependent records first
        passwordResetTokenRepository.deleteAll();
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setFullName("Password Test User");
        testUser.setEmail("passwordtest@example.com");
        testUser.setPassword(passwordEncoder.encode("oldPassword123"));
        testUser.setRole("ROLE_USER");

        testUser = userRepository.save(testUser);

        reset(javaMailSender);
    }

    // 1. Valid token should update password and delete token

    @Test
    void resetPassword_withValidToken_updatesPassword() throws Exception {

        String token = "valid-reset-token";

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                testUser,
                new Date(System.currentTimeMillis() + 600000)
        );

        passwordResetTokenRepository.save(resetToken);

        String request = """
                {
                    "token": "valid-reset-token",
                    "password": "newPassword123"
                }
                """;

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Password updated successfully."))
                .andExpect(jsonPath("$.status").value(true));

        User updatedUser = userRepository.findById(testUser.getId())
                .orElseThrow();

        assertTrue(passwordEncoder.matches(
                "newPassword123",
                updatedUser.getPassword()
        ));

        assertNull(passwordResetTokenRepository.findByToken(token));
    }

    // 2. Invalid token should be rejected

    @Test
    void resetPassword_withInvalidToken_returnsClientError() throws Exception {

        String request = """
                {
                    "token": "invalid-token",
                    "password": "newPassword123"
                }
                """;

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().is4xxClientError());

        User unchangedUser = userRepository.findById(testUser.getId())
                .orElseThrow();

        assertTrue(passwordEncoder.matches(
                "oldPassword123",
                unchangedUser.getPassword()
        ));
    }

    // 3. Expired token should be rejected and deleted

    @Test
    void resetPassword_withExpiredToken_deletesToken() throws Exception {

        String token = "expired-reset-token";

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                testUser,
                new Date(System.currentTimeMillis() - 600000)
        );

        passwordResetTokenRepository.save(resetToken);

        String request = """
                {
                    "token": "expired-reset-token",
                    "password": "newPassword123"
                }
                """;

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().is4xxClientError());

        assertNull(passwordResetTokenRepository.findByToken(token));
    }

    // 4. Existing email should generate a reset token and trigger email

    @Test
    void requestPasswordReset_withExistingEmail_sendsEmail() throws Exception {

        mockMvc.perform(post("/reset-password/reset")
                        .param("email", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Password reset email sent successfully."))
                .andExpect(jsonPath("$.status").value(true));

        PasswordResetToken savedToken =
                passwordResetTokenRepository.findByUser(testUser)
                        .orElseThrow();

        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.isExpired());

        verify(javaMailSender, times(1))
                .send(any(SimpleMailMessage.class));
    }

    // 5. Unknown email should be rejected

    @Test
    void requestPasswordReset_withUnknownEmail_returnsClientError() throws Exception {

        mockMvc.perform(post("/reset-password/reset")
                        .param("email", "unknown@example.com"))
                .andExpect(status().is4xxClientError());

        verify(javaMailSender, never())
                .send(any(SimpleMailMessage.class));
    }

    // 6. Requesting another reset should replace the old token

    @Test
    void requestPasswordReset_withExistingToken_replacesOldToken() throws Exception {

        PasswordResetToken oldToken = new PasswordResetToken(
                "old-reset-token",
                testUser,
                new Date(System.currentTimeMillis() + 600000)
        );

        passwordResetTokenRepository.save(oldToken);

        mockMvc.perform(post("/reset-password/reset")
                        .param("email", testUser.getEmail()))
                .andExpect(status().isOk());

        PasswordResetToken newToken =
                passwordResetTokenRepository.findByUser(testUser)
                        .orElseThrow();

        assertNotEquals("old-reset-token", newToken.getToken());
        assertNull(passwordResetTokenRepository.findByToken("old-reset-token"));

        verify(javaMailSender, times(1))
                .send(any(SimpleMailMessage.class));
    }
}
