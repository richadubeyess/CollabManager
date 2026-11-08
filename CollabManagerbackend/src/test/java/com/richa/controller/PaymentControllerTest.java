
package com.richa.controller;

import com.razorpay.PaymentLink;
import com.razorpay.PaymentLinkClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import com.richa.exception.UserException;
import com.richa.model.User;
import com.richa.response.PaymentLinkResponse;
import com.richa.service.UserService;

import org.json.JSONObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                paymentController, "apiKey", "test_key");

        ReflectionTestUtils.setField(
                paymentController, "apiSecret", "test_secret");

        ReflectionTestUtils.setField(
                paymentController, "frontendUrl", "http://localhost:5173");
    }

    @Test
    void createPaymentLink_monthlyPlan_returnsPaymentLink()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getFullName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");

        PaymentLinkClient paymentLinkClient =
                mock(PaymentLinkClient.class);

        PaymentLink payment = mock(PaymentLink.class);

        when(paymentLinkClient.create(any(JSONObject.class)))
                .thenReturn(payment);

        when(payment.get("id")).thenReturn("plink_test123");
        when(payment.get("short_url"))
                .thenReturn("https://rzp.io/test123");

        try (MockedConstruction<RazorpayClient> mocked =
                     mockConstruction(RazorpayClient.class,
                             (mock, context) ->
                                     ReflectionTestUtils.setField(
                                             mock,
                                             "paymentLink",
                                             paymentLinkClient))) {

            ResponseEntity<PaymentLinkResponse> response =
                    paymentController.createPaymentLink(
                            "MONTHLY", "Bearer token");

            assertEquals(
                    HttpStatus.ACCEPTED,
                    response.getStatusCode());

            assertNotNull(response.getBody());

            verify(paymentLinkClient)
                    .create(any(JSONObject.class));
        }
    }

    @Test
    void createPaymentLink_annuallyPlan_usesAnnualAmount()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getFullName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");

        PaymentLinkClient paymentLinkClient =
                mock(PaymentLinkClient.class);

        PaymentLink payment = mock(PaymentLink.class);

        when(paymentLinkClient.create(any(JSONObject.class)))
                .thenReturn(payment);

        when(payment.get("id")).thenReturn("plink_annual");
        when(payment.get("short_url"))
                .thenReturn("https://rzp.io/annual");

        try (MockedConstruction<RazorpayClient> mocked =
                     mockConstruction(RazorpayClient.class,
                             (mock, context) ->
                                     ReflectionTestUtils.setField(
                                             mock,
                                             "paymentLink",
                                             paymentLinkClient))) {

            ResponseEntity<PaymentLinkResponse> response =
                    paymentController.createPaymentLink(
                            "ANNUALLY", "Bearer token");

            ArgumentCaptor<JSONObject> captor =
                    ArgumentCaptor.forClass(JSONObject.class);

            verify(paymentLinkClient)
                    .create(captor.capture());

            JSONObject request = captor.getValue();

            assertEquals(671160, request.getInt("amount"));
            assertEquals("INR", request.getString("currency"));

            assertEquals(
                    "http://localhost:5173/upgrade_plan/success?planType=ANNUALLY",
                    request.getString("callback_url"));

            assertEquals(
                    HttpStatus.ACCEPTED,
                    response.getStatusCode());

            assertNotNull(response.getBody());
        }
    }

    @Test
    void createPaymentLink_whenUserNotFound_throwsUserException()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenThrow(new UserException("User not found"));

        assertThrows(
                UserException.class,
                () -> paymentController.createPaymentLink(
                        "MONTHLY", "Bearer token"));

        verifyNoInteractions(user);
    }

    @Test
    void createPaymentLink_whenRazorpayFails_throwsRazorpayException()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getFullName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");

        PaymentLinkClient paymentLinkClient =
                mock(PaymentLinkClient.class);

        when(paymentLinkClient.create(any(JSONObject.class)))
                .thenThrow(new RazorpayException("Payment failed"));

        try (MockedConstruction<RazorpayClient> mocked =
                     mockConstruction(RazorpayClient.class,
                             (mock, context) ->
                                     ReflectionTestUtils.setField(
                                             mock,
                                             "paymentLink",
                                             paymentLinkClient))) {

            assertThrows(
                    RazorpayException.class,
                    () -> paymentController.createPaymentLink(
                            "MONTHLY", "Bearer token"));

            verify(paymentLinkClient)
                    .create(any(JSONObject.class));
        }
    }
}
