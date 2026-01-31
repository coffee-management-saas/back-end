package com.futurenbetter.saas.modules.promotion.service;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest promotionRequest);

    PromotionResponse uploadImage(Long promotionId, MultipartFile image) throws IOException;

    List<PromotionResponse> getAllPromotions();

    PromotionResponse getPromotion(Long promotionId);

    PromotionResponse updatePromotion(Long promotionId, PromotionRequest promotionRequest);

    void deletePromotion(Long promotionId);

    Promotion validatePromotion(String promotionCode, Long shopId, Long customerId, Long orderAmount);

    void recordPromotionUsage(Long promotionId, Long customerId, Long shopId, Long discountAmount);
}
