package com.futurenbetter.saas.modules.promotion.controller;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("")
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionRequest promotionRequest) {
        PromotionResponse response = promotionService.createPromotion(promotionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
