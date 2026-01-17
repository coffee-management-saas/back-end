package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;

import java.util.List;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest);
    List<SubscriptionPlanResponse> getAllSubscriptionPlan();
}
