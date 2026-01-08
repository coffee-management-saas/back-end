package com.futurenbetter.saas.modules.promotion.repository;

import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
