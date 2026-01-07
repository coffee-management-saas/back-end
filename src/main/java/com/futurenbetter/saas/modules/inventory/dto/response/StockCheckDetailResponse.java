package com.futurenbetter.saas.modules.inventory.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckDetailResponse {
    Long ingredientId;
    String ingredientName;
    Integer snapshotQuantity;
    Integer actualQuantity;
    Integer diffQuantity;
    String reason;
}
