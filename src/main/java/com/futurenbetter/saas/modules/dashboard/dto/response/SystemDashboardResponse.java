package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemDashboardResponse {
    private Long totalSubscriptionRevenue;
    private Integer totalNewShops;
}
