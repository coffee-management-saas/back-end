package com.futurenbetter.saas.modules.order.controller;

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
    @Value("${GOONG_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String input) {
        String encodedInput = UriUtils.encode(input, StandardCharsets.UTF_8);
        String url = String.format("https://rsapi.goong.io/Place/AutoComplete?api_key=%s&input=%s", apiKey, encodedInput);
        try {
            // Sử dụng URI.create để tránh RestTemplate encode lần nữa những dấu % đã có
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
}
