package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
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
public class UnitConversion {
    Long id;
    Long shopId;
    Long rawIngredientId;
    InputUnit fromUnit;
    BaseUnit toUnit;
    Integer conversionFactor;
    Boolean isStandard;
    LocalDateTime fromDate; // created after this date
    LocalDateTime toDate; // updated before this date
    Status status;
}
