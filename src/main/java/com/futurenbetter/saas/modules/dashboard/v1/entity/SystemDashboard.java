package com.futurenbetter.saas.modules.dashboard.v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.Month;

@Entity
@Table(
        name = "system_dashboards",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
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
    private Long totalRevenue; // từ amount trong billing invoice

    @Column(name = "total_subscriptions")
    private Integer totalSubscriptions; // count trong shop_subscription với status = ACTIVE

    @Column(name = "new_shops")
    private Integer newShops; // count trong shop với created_at trong tháng và năm tương ứng

    @Column(name = "returning_shops")
    private Integer returningShops; // count trong shop với created_at trước tháng và năm tương ứng, nhưng có shop_subscription với created_at trong tháng và năm tương ứng

    @Column(name = "total_expenses")
    private Long totalExpenses; // tổng chi phí vận hành hệ thống trong tháng, có thể lấy từ một bảng khác nếu có

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
