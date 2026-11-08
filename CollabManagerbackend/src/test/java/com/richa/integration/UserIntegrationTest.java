
package com.richa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.richa.model.User;

import com.richa.repository.CommentRepository;
import com.richa.repository.ChatRepository;
import com.richa.repository.IssueRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private String jwt;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {

        // Clean test data in dependency order.
        commentRepository.deleteAll();
        issueRepository.deleteAll();
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Register a test user.
        User user = new User();
        user.setFullName("User Integration Test");
        user.setEmail("usertest@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract JWT from signup response.
        JsonNode signupJson = objectMapper.readTree(signupResponse);
        jwt = signupJson.get("jwt").asText();

        // Get the registered user ID.
        User savedUser =
                userRepository.findByEmail("usertest@example.com");

        userId = savedUser.getId();
    }

    // Test 1: Get authenticated user's profile.
    @Test
    void getUserProfile_returnsAuthenticatedUser() throws Exception {

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.fullName")
                        .value("User Integration Test"))
                .andExpect(jsonPath("$.email")
                        .value("usertest@example.com"))
                .andExpect(jsonPath("$.password").value((Object) null));
    }

    // Test 2: Find user by ID.
    @Test
    void findUserById_returnsUserDetails() throws Exception {

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.fullName")
                        .value("User Integration Test"))
                .andExpect(jsonPath("$.email")
                        .value("usertest@example.com"))
                .andExpect(jsonPath("$.password").value((Object) null));
    }
}
