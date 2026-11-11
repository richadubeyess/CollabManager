
package com.richa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.richa.model.Project;
import com.richa.model.User;
import com.richa.model.Chat;

import com.richa.repository.ProjectRepository;
import com.richa.repository.UserRepository;
import com.richa.repository.ChatRepository;
import com.richa.repository.SubscriptionRepository;

import com.richa.response.AuthResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

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
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private String jwt;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {

        // Clean up existing data in dependency order
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Register a fresh user through the actual signup API
        User user = new User();
        user.setFullName("Project Test User");
        user.setEmail("projecttest@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse =
                objectMapper.readValue(response, AuthResponse.class);

        jwt = authResponse.getJwt();

        testUser = userRepository.findByEmail("projecttest@example.com");

        assertNotNull(testUser);
        assertNotNull(jwt);
    }

    private Project createProjectRequest() {

        Project project = new Project();

        project.setName("Integration Test Project");
        project.setDescription("Testing project integration");
        project.setCategory("Development");
        project.setTags(Arrays.asList("Java", "Spring Boot"));

        return project;
    }

    // Test 1: Create project successfully
    @Test
    void createProject_withValidDetails_createsProject() throws Exception {

        Project project = createProjectRequest();

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name")
                        .value("Integration Test Project"))
                .andExpect(jsonPath("$.description")
                        .value("Testing project integration"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Project createdProject =
                objectMapper.readValue(response, Project.class);

        assertTrue(
                projectRepository.existsById(createdProject.getId())
        );
    }

    // Test 2: Verify project owner, team and project count
    @Test
    void createProject_assignsOwnerAndAddsOwnerToTeam() throws Exception {

        Project project = createProjectRequest();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner.id").value(testUser.getId()))
                .andExpect(jsonPath("$.team[0].id").value(testUser.getId()));

        User updatedUser =
                userRepository.findByEmail("projecttest@example.com");

        assertEquals(1, updatedUser.getProjectSize());
    }

    // Test 3: Verify associated chat is created
    @Test
    void createProject_createsAssociatedChat() throws Exception {

        Project project = createProjectRequest();

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Project createdProject =
                objectMapper.readValue(response, Project.class);

        Chat savedChat = chatRepository.findAll().stream()
                .filter(chat -> chat.getProject() != null
                        && chat.getProject().getId()
                        .equals(createdProject.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedChat);
    }

    // Test 4: Fetch project by ID
    @Test
    void getProjectById_withExistingId_returnsProject() throws Exception {

        Project project = createProjectRequest();

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Project createdProject =
                objectMapper.readValue(response, Project.class);

        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProject.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Integration Test Project"));
    }

    // Test 5: Update project
    @Test
    void updateProject_withValidDetails_updatesProject() throws Exception {

        Project project = createProjectRequest();

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Project createdProject =
                objectMapper.readValue(response, Project.class);

        Project updatedProject = new Project();

        updatedProject.setName("Updated Integration Project");
        updatedProject.setDescription("Updated description");
        updatedProject.setTags(Arrays.asList("Java", "Testing"));

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Updated Integration Project"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));

        Project savedProject = projectRepository
                .findById(createdProject.getId())
                .orElseThrow();

        assertEquals("Updated Integration Project", savedProject.getName());
        assertEquals("Updated description", savedProject.getDescription());
        assertEquals(Arrays.asList("Java", "Testing"), savedProject.getTags());
    }

    // Test 6: Delete project
    @Test
    void deleteProject_withExistingId_deletesProject() throws Exception {

        Project project = createProjectRequest();

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Project createdProject =
                objectMapper.readValue(response, Project.class);

        Long projectId = createdProject.getId();

        assertEquals(1, userRepository
                .findByEmail("projecttest@example.com")
                .getProjectSize());

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        assertFalse(projectRepository.existsById(projectId));

        User updatedUser =
                userRepository.findByEmail("projecttest@example.com");

        assertEquals(0, updatedUser.getProjectSize());
    }

}
