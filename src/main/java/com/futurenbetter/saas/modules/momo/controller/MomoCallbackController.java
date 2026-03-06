package com.futurenbetter.saas.modules.momo.controller;

import com.futurenbetter.saas.common.utils.MomoUtils;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoCallbackController {

    private final MomoUtils momoUtils;
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.frontend-url}")
    private String frontendUrl;

    @PostMapping("/ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody Map<String, Object> payload) {
        Map<String, String> stringPayload = new HashMap<>();
        payload.forEach((k, v) -> stringPayload.put(k, v != null ? String.valueOf(v) : null));

        String extraData = stringPayload.get("extraData");
        String type = momoUtils.decodeType(extraData);

        if ("SUBSCRIPTION".equals(type)) {
            subscriptionService.handleMomoIpn(stringPayload);
        } else if ("ORDER".equals(type)
                || (stringPayload.get("orderId") != null && stringPayload.get("orderId").startsWith("ORD_"))) {
            orderService.handleMomoOrderIpn(stringPayload);
        } else {
            log.warn("Unknown IPN type: {}. Payload keys: {}", type, stringPayload.keySet());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleMomoRedirect(
            @RequestParam Map<String, String> params) {
        String extraData = params.get("extraData");
        String type = momoUtils.decodeType(extraData);
        String resultCode = params.get("resultCode");

        if ("SUBSCRIPTION".equals(type)) {
            String targetUrl = frontendUrl + "/subscription/momo-callback";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetUrl))
                    .build();
        } else if ("ORDER".equals(type)
                || (params.get("orderId") != null && params.get("orderId").startsWith("ORD_"))) {
            String orderId = params.getOrDefault("orderId", "");

            if ("0".equals(resultCode)) {
                try {
                    orderService.handleMomoOrderIpn(params);
                } catch (Exception e) {
                }
            } else {
                log.warn("Payment failed for orderId: {}, resultCode: {}", orderId, resultCode);
            }

            String targetUrl = frontendUrl + "/checkout?resultCode=" + (resultCode != null ? resultCode : "")
                    + "&orderId="
                    + orderId;

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetUrl))
                    .build();
        }

        return ResponseEntity.badRequest().body("Invalid request");
    }
}
