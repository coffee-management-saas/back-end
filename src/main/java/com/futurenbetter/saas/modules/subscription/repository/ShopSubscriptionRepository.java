package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.ShopSubscription;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ShopSubscriptionRepository extends JpaRepository<ShopSubscription, Long> {
    Integer countBySubscriptionPlanStatusAndCreatedAtBetween(SubscriptionPlanEnum subscriptionPlanStatus, LocalDateTime fromDate, LocalDateTime toDate);

    @Query("SELECT COUNT(DISTINCT s.shop.id) " +
            "FROM ShopSubscription s " +
            "WHERE s.shop.id IN (" +
            "    SELECT sub.shop.id " +
            "    FROM ShopSubscription sub " +
            "    GROUP BY sub.shop.id " +
            "    HAVING COUNT(sub.shopSubscriptionId) > 1 " +
            "    AND MAX(sub.createdAt) BETWEEN :fromDate AND :toDate" +
            ")")
    Integer countReturningShop(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
