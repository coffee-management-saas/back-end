package com.futurenbetter.saas.modules.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MonthlyProductSoldResponse {
    private Long id;
    private Long shopId;
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private String month;
    private Integer year;
}
