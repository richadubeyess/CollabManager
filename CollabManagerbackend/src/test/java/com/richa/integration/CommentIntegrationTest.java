
package com.richa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.richa.model.User;
import com.richa.model.Project;
import com.richa.request.IssueRequest;

import com.richa.repository.CommentRepository;
import com.richa.repository.IssueRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.ChatRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

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
class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

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
    private Long userId;
    private Long projectId;
    private Long issueId;

    @BeforeEach
    void setUp() throws Exception {

        // Clean existing test data.
        commentRepository.deleteAll();
        issueRepository.deleteAll();
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Register a test user.
        User user = new User();
        user.setFullName("Comment Test User");
        user.setEmail("commenttest@example.com");
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

        User savedUser =
                userRepository.findByEmail("commenttest@example.com");

        userId = savedUser.getId();

        // Create a project.
        Project project = new Project();
        project.setName("Comment Test Project");
        project.setDescription("Project for comment integration tests");
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

        // Create an issue for the project.
        IssueRequest issueRequest = new IssueRequest();

        issueRequest.setTitle("Comment Test Issue");
        issueRequest.setDescription("Issue for comment integration tests");
        issueRequest.setStatus("TODO");
        issueRequest.setProjectId(projectId);
        issueRequest.setPriority("HIGH");
        issueRequest.setDueDate(LocalDate.now().plusDays(7));

        String issueResponse = mockMvc.perform(post("/api/issues")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issueRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode issueJson = objectMapper.readTree(issueResponse);
        issueId = issueJson.get("id").asLong();
    }

    // Helper method to create a comment through the API.
    private Long createCommentThroughApi() throws Exception {

        Map<String, Object> request = Map.of(
                "issueId", issueId,
                "content", "This is an integration test comment"
        );

        String response = mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("id").asLong();
    }

    // 1. Create comment.
    @Test
    void createComment_withValidDetails_createsComment() throws Exception {

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "issueId", issueId,
                                        "content", "This is a test comment"
                                )
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content")
                        .value("This is a test comment"));
    }

    // 2. Get comments by issue ID.
    @Test
    void getCommentsByIssueId_returnsIssueComments() throws Exception {

        createCommentThroughApi();

        mockMvc.perform(get("/api/comments/" + issueId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content")
                        .value("This is an integration test comment"));
    }

    // 3. Delete comment.
    @Test
    void deleteComment_withOwnerPermission_deletesComment() throws Exception {

        Long commentId = createCommentThroughApi();

        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("comment deleted successfully"));

        assertFalse(commentRepository.findById(commentId).isPresent());
    }
}
