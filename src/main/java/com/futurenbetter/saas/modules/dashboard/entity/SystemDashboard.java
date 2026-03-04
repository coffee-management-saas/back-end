package com.futurenbetter.saas.modules.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Month;

@Entity
@Table(
        name = "system_dashboards",
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
public class SystemDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month", nullable = false)
    private Month month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_revenue")
    private Long totalRevenue;

    @Column(name = "total_subscriptions")
    private Integer totalSubscriptions;

    @Column(name = "new_shops")
    private Integer newShops;

    @Column(name = "returning_shops")
    private Integer returningShops;

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
