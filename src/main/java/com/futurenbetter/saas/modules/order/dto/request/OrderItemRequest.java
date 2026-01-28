package com.futurenbetter.saas.modules.order.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {

    Long productVariantId;
    Integer quantity;
    Long sizeId;
    List<ToppingItemRequest> toppingItems;
}
