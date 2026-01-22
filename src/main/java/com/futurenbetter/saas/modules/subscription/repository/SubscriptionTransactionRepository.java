package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionTransactionEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, Long> {
    Optional<SubscriptionTransaction> findByOrderId(String orderId);
    List<SubscriptionTransaction> findAllByStatusAndCreatedAtBefore(
            SubscriptionTransactionEnum status,
            LocalDateTime dateTime
    );
}
