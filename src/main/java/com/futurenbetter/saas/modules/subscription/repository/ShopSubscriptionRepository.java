package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.ShopSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopSubscriptionRepository extends JpaRepository<ShopSubscription, Long> {
}
