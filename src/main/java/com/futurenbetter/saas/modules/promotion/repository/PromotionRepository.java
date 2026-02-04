package com.futurenbetter.saas.modules.promotion.repository;

import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findAllByShopIdAndPromotionStatus(Long shopId, PromotionEnum status);

    Optional<Promotion> findByPromotionCodeAndShopId(String promotionCode, Long shopId);

    @Query("SELECT p FROM Promotion p " +
            "LEFT JOIN FETCH p.promotionTargets pt " +
            "LEFT JOIN FETCH pt.product " +
            "WHERE p.promotionCode = :promotionCode AND p.shop.id = :shopId")
    Optional<Promotion> findByPromotionCodeAndShopIdWithTargets(
            @Param("promotionCode") String promotionCode,
            @Param("shopId") Long shopId);
}
