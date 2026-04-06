package com.futurenbetter.saas.modules.dashboard.v1.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.Month;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemDashboardResponse {
    Long id;
    Month month;
    Integer year;
    Long totalRevenue;
    Integer totalSubscriptions;
    Integer newShops;
    Integer returningShops;
    Long totalExpenses;
    LocalDateTime createdAt;
}
