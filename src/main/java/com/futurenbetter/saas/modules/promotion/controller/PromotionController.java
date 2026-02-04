package com.futurenbetter.saas.modules.promotion.controller;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromotionResponse> uploadPromotionImage(
            @PathVariable("id") Long id,
            @RequestParam("image") MultipartFile image) throws IOException {
        PromotionResponse response = promotionService.uploadImage(id, image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        List<PromotionResponse> response = promotionService.getAllPromotions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long promotionId) {
        PromotionResponse response = promotionService.getPromotion(promotionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{promotionId}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long promotionId,
                                                             @Valid @RequestBody PromotionRequest promotionRequest) {
        PromotionResponse response = promotionService.updatePromotion(promotionId, promotionRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{promotionId}")
    public ResponseEntity<String> deletePromotionById(@PathVariable Long promotionId) {
        promotionService.deletePromotion(promotionId);
        return new ResponseEntity<>("Xóa mã khuyến mãi thành công", HttpStatus.OK);
    }
}
