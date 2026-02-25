package com.futurenbetter.saas.modules.dashboard.dto.response;

import com.futurenbetter.saas.modules.dashboard.dto.projection.TopProductProjection;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TopSellingProductResponse {
    private String productName;
    private Integer totalUnitsSold;
    private List<TopProductProjection> topProducts;
}
