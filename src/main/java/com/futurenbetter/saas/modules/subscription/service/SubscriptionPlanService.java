package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;

import java.util.List;
import java.util.Map;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest);
    List<SubscriptionPlanResponse> getAllSubscriptionPlan();
    SubscriptionPlanResponse getSubscriptionPlanById(Long id);
    SubscriptionPlanResponse updateSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest, Long id);
    void deleteSubscriptionPlan(Long id);

}
