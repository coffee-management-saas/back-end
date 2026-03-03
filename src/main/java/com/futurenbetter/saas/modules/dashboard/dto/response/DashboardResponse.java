package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {
    Long id;
    Long shopId;
    String month;
    Integer year;
    Double totalRevenue;
    Integer totalOrders;
    Integer totalProduct;
    Integer newCustomers;
    Integer returningCustomers;
    Integer totalOfflineOrders;
    Integer totalOnlineOrders;
}
