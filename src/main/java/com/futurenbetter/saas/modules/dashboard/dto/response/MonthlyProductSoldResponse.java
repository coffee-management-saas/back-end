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
public class MonthlyProductSoldResponse {
    Long id;
    Long shopId;
    Long productId;
    String productName;
    Integer quantitySold;
    String month;
    Integer year;
}
