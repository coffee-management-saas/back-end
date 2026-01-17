package com.futurenbetter.saas.modules.subscription.dto.response;

import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanResponse {
    private Long subscriptionPlanId;
    private String subscriptionPlanName;
    private String subscriptionPlanDescription;
    private Long priceMonthly;
    private Long priceYearly;
    private Map<String, Object> configLimit;
    private SubscriptionPlanEnum subscriptionPlanStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

