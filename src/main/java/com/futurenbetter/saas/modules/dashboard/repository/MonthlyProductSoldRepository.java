package com.futurenbetter.saas.modules.dashboard.repository;

import com.futurenbetter.saas.modules.dashboard.entity.MonthlyProductSold;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface MonthlyProductSoldRepository extends JpaRepository<MonthlyProductSold, Long>, JpaSpecificationExecutor<MonthlyProductSold> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MonthlyProductSold> findByShopIdAndProductIdAndYearMonth(Long shopId, Long productId, YearMonth yearMonth);

    @Query(value = """
            WITH RankedProducts AS (
                SELECT *,
                       ROW_NUMBER() OVER(PARTITION BY month ORDER BY quantity_sold DESC) as row_num
                FROM monthly_product_sold
                WHERE shop_id = :shopId 
                  AND YEAR(month) = :year
            )
            SELECT * FROM RankedProducts 
            WHERE row_num <= :limit
            """, nativeQuery = true)
    List<MonthlyProductSold> findTopProductsPerMonthByYear(
            @Param("shopId") Long shopId,
            @Param("year") Integer year,
            @Param("limit") Integer limit);
}
