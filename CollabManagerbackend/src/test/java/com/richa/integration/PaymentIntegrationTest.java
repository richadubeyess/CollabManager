
package com.richa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.richa.model.User;
import com.richa.repository.ChatRepository;
import com.richa.repository.CommentRepository;
import com.richa.repository.IssueRepository;
import com.richa.repository.ProjectRepository;
import com.richa.repository.SubscriptionRepository;
import com.richa.repository.UserRepository;

import org.json.JSONObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
        "razorpay.api.key=test_key",
        "razorpay.api.secret=test_secret",
        "app.frontend.url=http://localhost:5173"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIntegrationTest {

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
        user.setFullName("Payment Test User");
        user.setEmail("paymenttest@example.com");
        user.setPassword("password123");
        user.setRole("ROLE_USER");

        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract JWT.
        JsonNode json = objectMapper.readTree(response);
        jwt = json.get("jwt").asText();
    }

    /*
     * Mock RazorpayClient construction.
     * This prevents real Razorpay API requests.
     */
    private MockedConstruction<RazorpayClient> mockRazorpay(
            PaymentLink paymentLink,
            AtomicReference<JSONObject> capturedRequest
    ) throws Exception {

        return Mockito.mockConstruction(
                RazorpayClient.class,
                (razorpayClient, context) -> {

                    Field paymentLinkField =
                            RazorpayClient.class.getField("paymentLink");

                    Class<?> paymentLinkClientType =
                            paymentLinkField.getType();

                    Object paymentLinkClientMock = Mockito.mock(
                            paymentLinkClientType,
                            invocation -> {

                                if (invocation.getMethod()
                                        .getName().equals("create")) {

                                    capturedRequest.set(
                                            invocation.getArgument(0)
                                    );

                                    return paymentLink;
                                }

                                return Answers.RETURNS_DEFAULTS.answer(invocation);
                            }
                    );

                    paymentLinkField.set(
                            razorpayClient,
                            paymentLinkClientMock
                    );
                }
        );
    }

    // Test 1: Monthly payment link creation.
    @Test
    void createMonthlyPaymentLink_returnsPaymentLink() throws Exception {

        PaymentLink paymentLink = mock(PaymentLink.class);

        when(paymentLink.get("id"))
                .thenReturn("plink_monthly_test");

        when(paymentLink.get("short_url"))
                .thenReturn("https://rzp.io/test-monthly");

        AtomicReference<JSONObject> capturedRequest =
                new AtomicReference<>();

        try (MockedConstruction<RazorpayClient> mocked =
                     mockRazorpay(paymentLink, capturedRequest)) {

            mockMvc.perform(post("/api/payments/MONTHLY")
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.payment_link_id")
                            .value("plink_monthly_test"))
                    .andExpect(jsonPath("$.payment_link_url")
                            .value("https://rzp.io/test-monthly"));

            assertEquals(
                    79900,
                    capturedRequest.get().getInt("amount")
            );

            assertEquals(
                    "INR",
                    capturedRequest.get().getString("currency")
            );

            assertEquals(1, mocked.constructed().size());
        }
    }

    // Test 2: Annual payment link creation.
    @Test
    void createAnnualPaymentLink_usesAnnualAmount() throws Exception {

        PaymentLink paymentLink = mock(PaymentLink.class);

        when(paymentLink.get("id"))
                .thenReturn("plink_annual_test");

        when(paymentLink.get("short_url"))
                .thenReturn("https://rzp.io/test-annual");

        AtomicReference<JSONObject> capturedRequest =
                new AtomicReference<>();

        try (MockedConstruction<RazorpayClient> mocked =
                     mockRazorpay(paymentLink, capturedRequest)) {

            mockMvc.perform(post("/api/payments/ANNUALLY")
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.payment_link_id")
                            .value("plink_annual_test"))
                    .andExpect(jsonPath("$.payment_link_url")
                            .value("https://rzp.io/test-annual"));

            // 79900 * 0.7 * 12 = 671160
            assertEquals(
                    671160,
                    capturedRequest.get().getInt("amount")
            );

            assertEquals(1, mocked.constructed().size());
        }
    }

    // Test 3: Missing Authorization header.
    @Test
    void createPaymentLink_withoutAuthorization_isRejected()
            throws Exception {

        mockMvc.perform(post("/api/payments/MONTHLY"))
                .andExpect(status().is4xxClientError());
    }

    // Test 4: Razorpay failure.
    @Test
    void createPaymentLink_whenRazorpayFails_returnsBadRequest()
            throws Exception {

        AtomicReference<JSONObject> capturedRequest =
                new AtomicReference<>();

        try (MockedConstruction<RazorpayClient> mocked =
                     Mockito.mockConstruction(
                             RazorpayClient.class,
                             (razorpayClient, context) -> {

                                 Field paymentLinkField =
                                         RazorpayClient.class.getField("paymentLink");

                                 Class<?> clientType =
                                         paymentLinkField.getType();

                                 Object clientMock = Mockito.mock(
                                         clientType,
                                         invocation -> {

                                             if (invocation.getMethod()
                                                     .getName().equals("create")) {

                                                 capturedRequest.set(
                                                         invocation.getArgument(0)
                                                 );

                                                 throw new RazorpayException(
                                                         "Simulated Razorpay failure"
                                                 );
                                             }

                                             return Answers.RETURNS_DEFAULTS
                                                     .answer(invocation);
                                         }
                                 );

                                 paymentLinkField.set(
                                         razorpayClient,
                                         clientMock
                                 );
                             }
                     )) {

            mockMvc.perform(post("/api/payments/MONTHLY")
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error")
                            .value("Simulated Razorpay failure"));

            assertEquals(1, mocked.constructed().size());
        }
    }
}
