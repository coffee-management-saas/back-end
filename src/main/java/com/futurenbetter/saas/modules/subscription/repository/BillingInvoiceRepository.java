package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.enums.InvoiceEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, Long> {
    @Query("SELECT COALESCE(SUM(b.amount), 0L) FROM BillingInvoice b " +
            "WHERE b.status = :status " +
            "AND b.createdAt >= :fromDate AND b.createdAt <= :toDate")
    Long sumAmountByStatusAndDateRange(
            @Param("status") InvoiceEnum status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
