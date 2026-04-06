package com.futurenbetter.saas.modules.dashboard.v1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "system_monthly_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "report_month",
                        "report_year"
                })
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_month", nullable = false)
    private Integer reportMonth;

    @Column(name = "report_year", nullable = false)
    private Integer reportYear;

    @Column(name = "total_revenue")
    private Long totalRevenue;

    @Column(name = "new_shops_count")
    private Integer newShopsCount;
}
