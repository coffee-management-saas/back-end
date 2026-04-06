package com.futurenbetter.saas.modules.dashboard.v1.dto.filter;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardFilter {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer topProductsLimit;
}
