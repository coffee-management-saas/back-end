package com.futurenbetter.saas.modules.dashboard.v1.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDailyProductResponse {
    private String productName;
    private Integer quantitySold;
}
