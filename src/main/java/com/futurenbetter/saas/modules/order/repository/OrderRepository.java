package com.futurenbetter.saas.modules.order.repository;

import com.futurenbetter.saas.modules.dashboard.v1.dto.projection.BestSellerProjection;
import com.futurenbetter.saas.modules.dashboard.v1.dto.projection.TopProductProjection;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

        @Query("SELECT COALESCE(SUM(o.paidPrice), 0L) FROM Order o " +
                        "WHERE o.shop.id = :shopId " +
                        "AND o.orderStatus = :status " +
                        "AND o.createdAt >= :fromDate AND o.createdAt <= :toDate")
        Long calculateTotalRevenueByShop(
                        @Param("shopId") Long shopId,
                        @Param("status") OrderStatus status, // Truyền trực tiếp Enum vào
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.createdAt >= :fromDate AND o.createdAt <= :toDate AND o.orderStatus = :status")
        Integer countOrdersByShop(@Param("shopId") Long shopId, @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate, @Param("status") OrderStatus status);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.createdAt >= :fromDate AND o.createdAt <= :toDate AND o.promotion.promotionId IS NOT NULL")
        Integer countOdersByShopIdAndHasPromotionIsTrue(@Param("shopId") Long shopId,
                        @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

        @Query("SELECT p.id AS productId, p.name AS productName, SUM(oi.quantity) AS totalQuantity " +
                        "FROM Order o " +
                        "JOIN o.orderItems oi " +
                        "JOIN oi.productVariant pv " + // Đi qua variant nếu project của bạn map OrderItem ->
                                                       // ProductVariant -> Product
                        "JOIN pv.product p " +
                        "WHERE o.shop.id = :shopId " +
                        "AND o.orderStatus = 'COMPLETED' " + // Chỉ tính các đơn đã hoàn thành
                        "AND o.createdAt >= :fromDate AND o.createdAt <= :toDate " +
                        "GROUP BY p.id, p.name " +
                        "ORDER BY totalQuantity DESC")
        List<TopProductProjection> findTopSellingProducts(
                        @Param("shopId") Long shopId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        @Query("SELECT p.id AS productId, " +
                        "p.name AS productName, " +
                        "p.image AS productImage, " +
                        "COALESCE(p.price, MIN(pv.price)) AS price, " +
                        "SUM(oi.quantity) AS totalQuantity, " +
                        "SUM(CAST(oi.quantity AS long) * oi.unitPrice) AS totalRevenue " +
                        "FROM Order o " +
                        "JOIN o.orderItems oi " +
                        "JOIN oi.productVariant pv " +
                        "JOIN pv.product p " +
                        "WHERE o.shop.id = :shopId " +
                        "AND o.orderStatus = 'PAID' " +
                        "AND o.createdAt >= :tenDaysAgo " +
                        "GROUP BY p.id, p.name, p.image, p.price, p.createdAt " +
                        "ORDER BY SUM(oi.quantity) DESC, " +
                        "SUM(CAST(oi.quantity AS long) * oi.unitPrice) DESC, " +
                        "p.createdAt DESC")
        List<BestSellerProjection> getBestSellerProducts(
                        @Param("shopId") Long shopId,
                        @Param("tenDaysAgo") LocalDateTime tenDaysAgo,
                        Pageable pageable);

        Integer countAllByOrderStatusAndOrderTypeAndShopIdAndCreatedAtBetween(OrderStatus status, OrderType type,
                        Long shopId, LocalDateTime fromDate, LocalDateTime toDate);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @Query("UPDATE Order o SET o.orderStatus = :status, o.updatedAt = :updatedAt WHERE o.orderId = :orderId")
        void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status,
                        @Param("updatedAt") LocalDateTime updatedAt);
}
