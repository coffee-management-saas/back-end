package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.enums.StorageType;
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
public class RawIngredientFilter {
    Long id;
    Long shopId;
    String name;
    String skuCode;
    BaseUnit baseUnit;
    Integer minStockAlert;
    Integer maxStockAlert;
    StorageType storageType;
    LocalDateTime fromDate; // created after this date
    LocalDateTime toDate; // updated before this date
    Status status;
}
