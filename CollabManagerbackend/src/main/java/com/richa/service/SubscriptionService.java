package com.richa.service;

import com.richa.domain.PlanType;
import com.richa.model.Subscription;
import com.richa.model.User;

public interface SubscriptionService {
    Subscription createSubscription(User user);
    Subscription getUserSubscription(Long userId) throws Exception;
    Subscription upgradeSubscription(Long userId, PlanType planType);

    boolean isValid(Subscription subscription);
}

