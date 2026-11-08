
package com.richa.controller;

import com.richa.domain.PlanType;
import com.richa.model.Subscription;
import com.richa.model.User;
import com.richa.service.SubscriptionService;
import com.richa.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @Mock
    private User user;

    @Mock
    private Subscription subscription;

    @Test
    void getUserSubscription_whenSubscriptionExists_returnsSubscription()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(subscriptionService.getUserSubscription(1L))
                .thenReturn(subscription);

        ResponseEntity<Subscription> response =
                subscriptionController.getUserSubscription("Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subscription, response.getBody());

        verify(subscriptionService).getUserSubscription(1L);
    }

    @Test
    void getUserSubscription_whenSubscriptionDoesNotExist_returnsNotFound()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(subscriptionService.getUserSubscription(1L))
                .thenReturn(null);

        ResponseEntity<Subscription> response =
                subscriptionController.getUserSubscription("Bearer token");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void upgradeSubscription_returnsUpgradedSubscription()
            throws Exception {

        when(userService.findUserProfileByJwt("Bearer token"))
                .thenReturn(user);

        when(user.getId()).thenReturn(1L);

        when(subscriptionService.upgradeSubscription(1L, PlanType.MONTHLY))
                .thenReturn(subscription);

        ResponseEntity<Subscription> response =
                subscriptionController.upgradeSubscription(
                        "Bearer token", PlanType.MONTHLY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subscription, response.getBody());

        verify(subscriptionService)
                .upgradeSubscription(1L, PlanType.MONTHLY);
    }
}
