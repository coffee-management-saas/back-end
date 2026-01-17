package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, Long> {
}
