package com.futurenbetter.saas.modules.dashboard.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Month;

@Entity
@Table(
        name = "dashboards",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "shop_id",
                        "month"
                })
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "month", nullable = false)
    private Month month;

    private Double totalRevenue;
    private Integer totalOrders;
    private Integer totalProduct;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Integer totalOfflineOrders;
    private Integer totalOnlineOrders;
}
