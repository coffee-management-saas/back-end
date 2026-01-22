package com.futurenbetter.saas.modules.product.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.product.enums.Status;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantFilter extends BaseFilter {
    Long productId;
    Long sizeId;
    String skuCode;
    Double minPrice;
    Double maxPrice;
    Status status;
}