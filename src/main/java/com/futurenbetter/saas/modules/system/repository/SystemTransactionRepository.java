package com.futurenbetter.saas.modules.system.repository;

import com.futurenbetter.saas.modules.system.entity.SystemTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemTransactionRepository extends JpaRepository<SystemTransaction, Long>, JpaSpecificationExecutor<SystemTransaction> {
    @Query("SELECT COALESCE(SUM(s.amount), 0L) FROM SystemTransaction s " +
            "WHERE s.createdAt >= :fromDate AND s.createdAt <= :toDate")
    Long sumAmountByCreatedAtBetween(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
