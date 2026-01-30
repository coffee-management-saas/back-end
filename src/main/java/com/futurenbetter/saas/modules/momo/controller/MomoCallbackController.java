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

    @PostMapping("/ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody Map<String, Object> payload) {
        Map<String, String> stringPayload = new HashMap<>();
        payload.forEach((k, v) -> stringPayload.put(k, v != null ? String.valueOf(v) : null));

        String extraData = stringPayload.get("extraData");
        String type = momoUtils.decodeType(extraData);

        if ("SUBSCRIPTION".equals(type)) {
            subscriptionService.handleMomoIpn(stringPayload);
        } else if ("ORDER".equals(type)) {
            orderService.handleMomoOrderIpn(stringPayload);
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
            String targetUrl = redirectUrl + "/subscription/momo-callback";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetUrl))
                    .build();
        } else if ("ORDER".equals(type)) {
            if ("0".equals(resultCode)) {
                return ResponseEntity.ok("Thành công");
            } else {
                return ResponseEntity.ok("Thất bại");
            }
        }

        return ResponseEntity.badRequest().body("Invalid request");
    }
}
