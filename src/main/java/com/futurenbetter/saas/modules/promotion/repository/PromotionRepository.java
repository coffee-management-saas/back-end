package com.futurenbetter.saas.modules.promotion.repository;

import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findAllByShopId(Long shopId);
}
