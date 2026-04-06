package com.futurenbetter.saas.modules.dashboard.v2.entity;

import com.futurenbetter.saas.modules.dashboard.v2.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_report_v2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopReportV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shopId;

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // DAY, WEEK, MONTH, YEAR

    private LocalDate reportDate;   // Dùng cho DAY
    private Integer weekNumber;     // 1-52
    private Integer month;          // 1-12
    private Integer year;

    private Double totalRevenue;
    private Long totalOrders;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}