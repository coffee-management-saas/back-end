package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.modules.inventory.enums.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientBatchFilter {
    Long id;
    Long ingredientId;
    Long shopId;
    String batchCode;
    String supplierName;
    LocalDateTime expiredBefore; // Filter for batches expiring before this date
    Integer currentQuantity;
    Double importPrice;
    LocalDateTime fromDate; // created after this date
    LocalDateTime toDate; // updated before this date
    Status status;
}
