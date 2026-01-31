package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientBatchFilter extends BaseFilter {
    Long ingredientId;
    String batchCode;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiredBeforeDate;
    Boolean hasRemainingQuantity;
    InventoryStatus inventoryStatus;
}