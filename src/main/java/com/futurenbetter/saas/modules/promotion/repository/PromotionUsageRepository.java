package com.futurenbetter.saas.modules.promotion.repository;

import com.futurenbetter.saas.modules.promotion.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {
    long countByPromotionPromotionIdAndCustomerId(Long promotionId, Long customerId);
}
