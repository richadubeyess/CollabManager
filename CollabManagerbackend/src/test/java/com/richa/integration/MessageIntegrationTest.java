
package com.richa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richa.model.User;
import com.richa.model.Project;
import com.richa.model.Chat;
import com.richa.model.Message;
import com.richa.repository.UserRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.ChatRepository;
import com.richa.repository.MessageRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.request.CreateMessageRequest;
import com.richa.response.AuthResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

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
class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private User testUser;
    private Project testProject;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        // Clean dependent records first
        messageRepository.deleteAll();
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Create user through the actual signup API
        User user = new User();
        user.setFullName("Message Test User");
        user.setEmail("message@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse =
                objectMapper.readValue(signupResponse, AuthResponse.class);

        jwt = authResponse.getJwt();

        assertNotNull(jwt);

        testUser = userRepository.findByEmail("message@example.com");
        assertNotNull(testUser);

        // Create project
        testProject = new Project();
        testProject.setName("Message Integration Project");
        testProject.setDescription("Project for message integration testing");
        testProject.setCategory("Development");
        testProject.setOwner(testUser);
        testProject.setTeam(new ArrayList<>());

        testProject = projectRepository.save(testProject);

        // Create chat for the project
        Chat chat = new Chat();
        chat.setName("Integration Test Chat");
        chat.setProject(testProject);
        chat.setMessages(new ArrayList<>());
        chat.setUsers(new ArrayList<>());

        chatRepository.save(chat);

        // Reload project and chat relationships
        testProject = projectRepository.findById(testProject.getId())
                .orElseThrow();

        assertNotNull(testProject.getId());
    }

    @Test
    void sendMessage_withValidDetails_createsMessage() throws Exception {

        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(testUser.getId());
        request.setProjectId(testProject.getId());
        request.setContent("Hello from integration testing");

        mockMvc.perform(post("/api/messages/send")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content")
                        .value("Hello from integration testing"));

        assertEquals(1, messageRepository.count());

        Message savedMessage = messageRepository.findAll().get(0);

        assertEquals("Hello from integration testing",
                savedMessage.getContent());

        assertEquals(testUser.getId(),
                savedMessage.getSender().getId());
    }

    @Test
    void getMessagesByProjectId_returnsProjectMessages() throws Exception {

        // Create a message through the API
        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(testUser.getId());
        request.setProjectId(testProject.getId());
        request.setContent("First integration message");

        mockMvc.perform(post("/api/messages/send")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Retrieve messages
        mockMvc.perform(get("/api/messages/chat/" + testProject.getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content")
                        .value("First integration message"));
    }

    @Test
    void sendMessage_withInvalidSender_returnsClientError() throws Exception {

        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(999999L);
        request.setProjectId(testProject.getId());
        request.setContent("Message with invalid sender");

        mockMvc.perform(post("/api/messages/send")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        assertEquals(0, messageRepository.count());
    }

    @Test
    void getMessagesByInvalidProject_returnsClientError() throws Exception {

        mockMvc.perform(get("/api/messages/chat/999999")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().is4xxClientError());
    }
}
