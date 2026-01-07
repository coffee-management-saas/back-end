package com.futurenbetter.saas.modules.inventory.dto.response;

import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.enums.StorageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RawIngredientResponse {
    Long id;
    String name;
    String skuCode;
    BaseUnit baseUnit;
    Integer minStockAlert;
    StorageType storageType;
    Double totalStockQuantity;
    Status status;
}
