package com.futurenbetter.saas.modules.subscription.controller;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/system/subscription-plan")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(
            @Valid @RequestBody SubscriptionPlanRequest subscriptionPlanRequest
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.createSubscriptionPlan(subscriptionPlanRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
