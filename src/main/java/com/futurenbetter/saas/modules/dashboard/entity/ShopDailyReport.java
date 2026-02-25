package com.futurenbetter.saas.modules.dashboard.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "shop_daily_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "shop_id",
                        "report_date"
                })
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_revenue")
    private Long totalRevenue;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "top_products_json", columnDefinition = "TEXT")
    private String topProductsJson;

    @Column(name = "using_vouchers_percentage")
    private Double usingVouchersPercentage;

}
