package com.futurenbetter.saas.modules.inventory.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.inventory.enums.StorageType;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RawIngredientFilter extends BaseFilter {
    String keyword;
    StorageType storageType;
    Boolean isLowStock;
    Status status;
}