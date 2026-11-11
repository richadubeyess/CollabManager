
package com.richa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.richa.model.User;
import com.richa.model.Subscription;
import com.richa.repository.ChatRepository;
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

import java.time.LocalDate;

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
class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChatRepository chatRepository;

    private String jwt;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {

        // Clean dependent records first
        chatRepository.deleteAll();
        projectRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // Register a user through the actual signup API
        User user = new User();
        user.setFullName("Subscription Test User");
        user.setEmail("subscriptiontest@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(response);
        jwt = responseJson.get("jwt").asText();

        assertNotNull(jwt);
        assertFalse(jwt.isBlank());

        testUser = userRepository.findByEmail(
                "subscriptiontest@example.com"
        );

        assertNotNull(testUser);
    }

    @Test
    void getUserSubscription_returnsFreeSubscription() throws Exception {

        mockMvc.perform(get("/api/subscriptions/user")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planType").value("FREE"))
                .andExpect(jsonPath("$.valid").value(true));

        Subscription subscription =
                subscriptionRepository.findByUserId(testUser.getId());

        assertNotNull(subscription);
        assertEquals("FREE", subscription.getPlanType().name());
        assertEquals("FREE", subscription.getSubscriptiontype().name());
        assertTrue(subscription.isValid());
    }

    @Test
    void upgradeSubscription_toMonthly_updatesSubscription() throws Exception {

        mockMvc.perform(patch("/api/subscriptions/upgrade")
                        .header("Authorization", "Bearer " + jwt)
                        .param("planType", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planType").value("MONTHLY"));

        Subscription subscription =
                subscriptionRepository.findByUserId(testUser.getId());

        assertNotNull(subscription);

        assertEquals("MONTHLY", subscription.getPlanType().name());
        assertEquals("PAID", subscription.getSubscriptiontype().name());

        assertTrue(subscription.isValid());

        assertEquals(
                LocalDate.now(),
                subscription.getSubscriptionStartDate()
        );

        assertEquals(
                LocalDate.now().plusMonths(1),
                subscription.getSubscriptionEndDate()
        );
    }

    @Test
    void upgradeSubscription_toAnnually_updatesSubscription() throws Exception {

        mockMvc.perform(patch("/api/subscriptions/upgrade")
                        .header("Authorization", "Bearer " + jwt)
                        .param("planType", "ANNUALLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planType").value("ANNUALLY"));

        Subscription subscription =
                subscriptionRepository.findByUserId(testUser.getId());

        assertNotNull(subscription);

        assertEquals("ANNUALLY", subscription.getPlanType().name());
        assertEquals("PAID", subscription.getSubscriptiontype().name());

        assertTrue(subscription.isValid());

        assertEquals(
                LocalDate.now(),
                subscription.getSubscriptionStartDate()
        );

        assertEquals(
                LocalDate.now().plusMonths(12),
                subscription.getSubscriptionEndDate()
        );
    }

    @Test
    void getUserSubscription_withoutAuthentication_isRejected()
            throws Exception {

        mockMvc.perform(get("/api/subscriptions/user"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void upgradeSubscription_withoutAuthentication_isRejected()
            throws Exception {

        mockMvc.perform(patch("/api/subscriptions/upgrade")
                        .param("planType", "MONTHLY"))
                .andExpect(status().is4xxClientError());
    }
}
