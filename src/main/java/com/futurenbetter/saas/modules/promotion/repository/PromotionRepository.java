package com.futurenbetter.saas.modules.promotion.repository;

import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findAllByShopIdAndPromotionStatus(Long shopId, PromotionEnum status);
}
