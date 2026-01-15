package com.futurenbetter.saas.modules.product.dto.response;

import com.futurenbetter.saas.modules.product.enums.Status;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long id;
    String productName;
    String sizeName;
    String sizeCode;
    Double price;
    Double costPrice;
    String skuCode;
    Status status;
}