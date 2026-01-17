package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllBySubscriptionPlanStatus(SubscriptionPlanEnum status);
}
