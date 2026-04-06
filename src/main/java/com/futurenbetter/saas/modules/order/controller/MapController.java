package com.futurenbetter.saas.modules.order.controller;

import com.futurenbetter.saas.modules.order.service.GoongMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {
    @Value("${goong.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final GoongMapService goongMapService;

    // Tọa độ Trường Đại học FPT TP.HCM
    private static final double SHOP_LAT = 10.8412;
    private static final double SHOP_LNG = 106.8098;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String input) {
        String encodedInput = UriUtils.encode(input, StandardCharsets.UTF_8);
        String url = String.format("https://rsapi.goong.io/Place/AutoComplete?api_key=%s&input=%s", apiKey,
                encodedInput);
        try {
            Object response = restTemplate.getForObject(URI.create(url), Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/place-detail")
    public ResponseEntity<?> getDetail(@RequestParam String placeId) {
        String url = String.format("https://rsapi.goong.io/Place/Detail?api_key=%s&place_id=%s", apiKey, placeId);
        try {
            Object response = restTemplate.getForObject(URI.create(url), Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String latlng) {
        String url = String.format("https://rsapi.goong.io/Geocode?api_key=%s&latlng=%s", apiKey, latlng);
        try {
            Object response = restTemplate.getForObject(URI.create(url), Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/calculate-shipping")
    public ResponseEntity<?> calculateShipping(@RequestParam double lat, @RequestParam double lng) {
        try {
            String origin = SHOP_LAT + "," + SHOP_LNG;
            String destination = lat + "," + lng;
            double distanceKm = goongMapService.getDistance(origin, destination);
            long shippingFee = goongMapService.calculateShippingFee(distanceKm);

            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("shopAddress", "Trường Đại học FPT TP.HCM, Khu Công nghệ cao, Thủ Đức");
            result.put("distanceKm", Math.round(distanceKm * 10.0) / 10.0);
            result.put("shippingFee", shippingFee);
            result.put("shippingFeeFormatted", String.format("%,dđ", shippingFee));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
