package com.futurenbetter.saas.modules.inventory.dto.response;

import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitConversionResponse {

    Long id;
    Long ingredientId;
    String ingredientName;
    InputUnit fromUnit;
    BaseUnit toUnit;
    Double conversionFactor;
    Boolean isStandard;
    InventoryStatus inventoryStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
