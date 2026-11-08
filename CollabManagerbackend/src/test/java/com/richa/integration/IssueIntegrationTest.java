
package com.richa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.richa.model.Issue;
import com.richa.model.Project;
import com.richa.model.User;
import com.richa.repository.ChatRepository;
import com.richa.repository.IssueRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.repository.UserRepository;
import com.richa.request.IssueRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

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
class IssueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwt;
    private Long projectId;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {

        // Clean up dependent records first.
        issueRepository.deleteAll();
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Register a test user.
        User user = new User();
        user.setFullName("Issue Test User");
        user.setEmail("issuetest@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String signupResponse = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode signupJson = objectMapper.readTree(signupResponse);
        jwt = signupJson.get("jwt").asText();

        // Find the registered user.
        User savedUser = userRepository.findByEmail("issuetest@example.com");

        userId = savedUser.getId();

        // Create a project for the test user.
        Project project = new Project();
        project.setName("Issue Test Project");
        project.setDescription("Project for issue integration tests");
        project.setCategory("Development");

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Java");
        tags.add("Spring");
        project.setTags(tags);

        String projectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode projectJson = objectMapper.readTree(projectResponse);
        projectId = projectJson.get("id").asLong();
    }

    // Creates the correct request DTO expected by IssueController.
    private IssueRequest createIssueRequest() {

        IssueRequest request = new IssueRequest();

        request.setTitle("Integration Test Issue");
        request.setDescription("Testing issue integration");
        request.setStatus("TODO");
        request.setProjectId(projectId);
        request.setPriority("HIGH");
        request.setDueDate(LocalDate.now().plusDays(7));

        return request;
    }

    // Creates an issue through the actual API.
    private Long createIssueThroughApi() throws Exception {

        String response = mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    @Test
    void createIssue_withValidDetails_createsIssue() throws Exception {

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIssueRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Issue"))
                .andExpect(jsonPath("$.description")
                        .value("Testing issue integration"));
    }

    @Test
    void getIssueById_returnsIssue() throws Exception {

        Long issueId = createIssueThroughApi();

        mockMvc.perform(get("/api/issues/" + issueId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId))
                .andExpect(jsonPath("$.title").value("Integration Test Issue"));
    }

    @Test
    void getIssuesByProjectId_returnsProjectIssues() throws Exception {

        createIssueThroughApi();

        mockMvc.perform(get("/api/issues/project/" + projectId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Integration Test Issue"));
    }

    @Test
    void updateIssue_withValidDetails_updatesIssue() throws Exception {

        Long issueId = createIssueThroughApi();

        IssueRequest updateRequest = createIssueRequest();
        updateRequest.setTitle("Updated Integration Issue");
        updateRequest.setDescription("Updated description");

        mockMvc.perform(put("/api/issues/" + issueId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated Integration Issue"));
    }

    @Test
    void deleteIssue_withValidId_deletesIssue() throws Exception {

        Long issueId = createIssueThroughApi();

        mockMvc.perform(delete("/api/issues/" + issueId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));

        org.junit.jupiter.api.Assertions.assertFalse(
                issueRepository.findById(issueId).isPresent()
        );
    }

    @Test
    void searchIssues_byTitle_returnsMatchingIssues() throws Exception {

        createIssueThroughApi();

        mockMvc.perform(get("/api/issues/search")
                        .header("Authorization", "Bearer " + jwt)
                        .param("title", "integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Integration Test Issue"));
    }

    @Test
    void getIssuesByAssigneeId_returnsAssignedIssues() throws Exception {

        // Create and persist an issue assigned to the test user.
        Issue issue = new Issue();
        issue.setTitle("Assigned Integration Issue");
        issue.setDescription("Testing assigned issues");
        issue.setStatus("TODO");
        issue.setPriority("HIGH");
        issue.setDueDate(LocalDate.now().plusDays(7));
        issue.setProjectID(projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        User user = userRepository.findById(userId)
                .orElseThrow();

        issue.setProject(project);
        issue.setAssignee(user);

        issueRepository.save(issue);

        mockMvc.perform(get("/api/issues/assignee/" + userId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Assigned Integration Issue"));
    }

    @Test
    void updateIssueStatus_changesStatus() throws Exception {

        Long issueId = createIssueThroughApi();

        mockMvc.perform(put("/api/issues/" + issueId + "/status/COMPLETED")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        Issue updatedIssue = issueRepository.findById(issueId)
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "COMPLETED", updatedIssue.getStatus()
        );
    }
}
