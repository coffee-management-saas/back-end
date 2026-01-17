package com.futurenbetter.saas.modules.subscription.controller;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/checkout")
    public ResponseEntity<MomoPaymentResponse> checkout(
            @Valid @RequestBody SubscriptionRequest request
            ) {
        MomoPaymentResponse response = subscriptionService.createSubscriptionWithMomo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/momo-callback")
    public ResponseEntity<?> momoCallback(
            @RequestParam Map<String, String> payload
            ) {
        subscriptionService.handleMomoIpn(payload);
        return ResponseEntity.ok("Thành công");
    }
}
