
package com.richa.service;

import com.richa.domain.PlanType;
import com.richa.domain.SubscriptionType;
import com.richa.model.Subscription;
import com.richa.model.User;
import com.richa.repository.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanType(PlanType.FREE);
        subscription.setSubscriptiontype(SubscriptionType.FREE);
        subscription.setSubscriptionStartDate(LocalDate.now());
        subscription.setSubscriptionEndDate(LocalDate.now().plusMonths(12));
        subscription.setValid(true);
    }

    @Test
    void createSubscription_returnsFreeSubscription() {
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.createSubscription(user);

        assertNotNull(result);
        assertEquals(PlanType.FREE, result.getPlanType());
        assertEquals(SubscriptionType.FREE, result.getSubscriptiontype());
        assertTrue(result.isValid());
        assertEquals(user, result.getUser());

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void getUserSubscription_whenSubscriptionExists_returnsSubscription() throws Exception {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.getUserSubscription(1L);

        assertNotNull(result);
        assertEquals(PlanType.FREE, result.getPlanType());
        assertTrue(result.isValid());

        verify(subscriptionRepository).findByUserId(1L);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void getUserSubscription_whenSubscriptionDoesNotExist_throwsException() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(null);

        Exception exception = assertThrows(
                Exception.class,
                () -> subscriptionService.getUserSubscription(1L)
        );

        assertTrue(exception.getMessage().contains("subscription not found"));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getUserSubscription_whenSubscriptionExpired_resetsToFreePlan() throws Exception {
        subscription.setSubscriptiontype(SubscriptionType.PAID);
        subscription.setPlanType(PlanType.ANNUALLY);
        subscription.setSubscriptionStartDate(LocalDate.now().minusMonths(13));
        subscription.setSubscriptionEndDate(LocalDate.now().minusDays(1));

        when(subscriptionRepository.findByUserId(1L)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.getUserSubscription(1L);

        assertEquals(PlanType.FREE, result.getPlanType());
        assertEquals(LocalDate.now(), result.getSubscriptionStartDate());
        assertEquals(LocalDate.now().plusMonths(12), result.getSubscriptionEndDate());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void upgradeSubscription_toAnnualPlan_updatesSubscription() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.upgradeSubscription(
                1L, PlanType.ANNUALLY
        );

        assertEquals(PlanType.ANNUALLY, result.getPlanType());
        assertEquals(SubscriptionType.PAID, result.getSubscriptiontype());
        assertEquals(LocalDate.now(), result.getSubscriptionStartDate());
        assertEquals(LocalDate.now().plusMonths(12), result.getSubscriptionEndDate());
        assertTrue(result.isValid());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void upgradeSubscription_toMonthlyPlan_updatesSubscription() {
        when(subscriptionRepository.findByUserId(1L)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.upgradeSubscription(
                1L, PlanType.MONTHLY
        );

        assertEquals(PlanType.MONTHLY, result.getPlanType());
        assertEquals(SubscriptionType.PAID, result.getSubscriptiontype());
        assertEquals(LocalDate.now().plusMonths(1), result.getSubscriptionEndDate());

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void isValid_whenSubscriptionIsFree_returnsTrue() {
        subscription.setSubscriptiontype(SubscriptionType.FREE);

        assertTrue(subscriptionService.isValid(subscription));
    }

    @Test
    void isValid_whenPaidSubscriptionIsExpired_returnsFalse() {
        subscription.setSubscriptiontype(SubscriptionType.PAID);
        subscription.setSubscriptionEndDate(LocalDate.now().minusDays(1));

        assertFalse(subscriptionService.isValid(subscription));
    }
}
