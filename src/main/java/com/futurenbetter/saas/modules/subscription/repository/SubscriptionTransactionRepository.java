package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionTransactionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, Long> {
    Optional<SubscriptionTransaction> findByOrderId(String orderId);
    List<SubscriptionTransaction> findAllByStatusAndCreatedAtBefore(
            SubscriptionTransactionEnum status,
            LocalDateTime dateTime
    );
    @Query("SELECT SUM(t.amount) FROM SubscriptionTransaction t WHERE t.status = 'SUCCESS' AND t.createdAt >= :fromDate AND t.createdAt <= :toDate")
    Long calculateTotalSystemRevenue(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
