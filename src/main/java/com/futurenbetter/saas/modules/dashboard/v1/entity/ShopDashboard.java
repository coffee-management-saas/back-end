package com.futurenbetter.saas.modules.dashboard.v1.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Month;

@Entity
@Table(
        name = "shop_dashboards",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "shop_id",
                        "year",
                        "month"
                })
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "month", nullable = false)
    private Month month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_revenue")
    private Long totalRevenue;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_product")
    private Integer totalProduct;

    @Column(name = "new_customers")
    private Integer newCustomers;

    @Column(name = "returning_customers")
    private Integer returningCustomers;

    @Column(name = "total_offline_orders")
    private Integer totalOfflineOrders;

    @Column(name = "total_online_orders")
    private Integer totalOnlineOrders;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
