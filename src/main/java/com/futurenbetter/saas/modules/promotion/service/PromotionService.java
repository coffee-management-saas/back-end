package com.futurenbetter.saas.modules.promotion.service;

import com.futurenbetter.saas.modules.promotion.dto.request.PromotionRequest;
import com.futurenbetter.saas.modules.promotion.dto.response.PromotionResponse;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest promotionRequest);
}
