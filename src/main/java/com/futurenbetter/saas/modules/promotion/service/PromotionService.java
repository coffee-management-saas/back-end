package com.futurenbetter.saas.modules.promotion.service;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest promotionRequest);
    List<PromotionResponse> getAllPromotions();
}
