package com.futurenbetter.saas.modules.subscription.controller;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlan() {
        List<SubscriptionPlanResponse> response = subscriptionPlanService.getAllSubscriptionPlan();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(
            @PathVariable long id
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.getSubscriptionPlanById(id);
        return ResponseEntity.ok(response);
    }
}
