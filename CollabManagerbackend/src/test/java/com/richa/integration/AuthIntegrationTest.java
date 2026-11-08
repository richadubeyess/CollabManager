
package com.richa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richa.model.User;
import com.richa.model.Subscription;
import com.richa.repository.ChatRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.UserRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.request.LoginRequest;
import com.richa.response.AuthResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
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
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @BeforeEach


        void setUp() {

            // Delete dependent records first
            chatRepository.deleteAll();
            projectRepository.deleteAll();

            // Delete subscriptions before users
            subscriptionRepository.deleteAll();
            userRepository.deleteAll();
        }


    @Test
    void signup_withValidDetails_createsUserAndSubscription()
            throws Exception {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.message")
                        .value("Register Success"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse =
                objectMapper.readValue(response, AuthResponse.class);

        assertNotNull(authResponse.getJwt());

        User savedUser = userRepository.findByEmail("test@example.com");

        assertNotNull(savedUser);
        assertEquals("Test User", savedUser.getFullName());

        Subscription subscription =
                subscriptionRepository.findByUserId(savedUser.getId());

        assertNotNull(subscription);
        assertEquals("FREE", subscription.getPlanType().name());
        assertTrue(subscription.isValid());
    }

    @Test
    void signup_withDuplicateEmail_returnsBadRequest()
            throws Exception {

        User existingUser = new User();
        existingUser.setFullName("Existing User");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("password123");
        existingUser.setRole("ROLE_USER");

        existingUser.setPassword(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode(existingUser.getPassword())
        );

        userRepository.save(existingUser);

        User duplicateUser = new User();
        duplicateUser.setFullName("Another User");
        duplicateUser.setEmail("duplicate@example.com");
        duplicateUser.setPassword("password456");
        duplicateUser.setRole("ROLE_USER");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signin_withValidCredentials_returnsJwt()
            throws Exception {

        User user = new User();
        user.setFullName("Login User");
        user.setEmail("login@example.com");
        user.setPassword(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode("password123")
        );
        user.setRole("ROLE_USER");

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").isNotEmpty())
                .andExpect(jsonPath("$.message")
                        .value("Login Success"));
    }

    @Test
    void signin_withIncorrectPassword_isRejected()
            throws Exception {

        User user = new User();
        user.setFullName("Login User");
        user.setEmail("wrongpass@example.com");
        user.setPassword(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode("correctPassword")
        );
        user.setRole("ROLE_USER");

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@example.com");
        loginRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }
}
