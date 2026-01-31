package com.futurenbetter.saas.modules.order.dto.response;

import com.futurenbetter.saas.modules.order.enums.OrderItemStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long orderItemId;
    Long unitPrice;
    Integer quantity;
    OrderItemStatus orderItemStatus;
    List<ToppingPerOrderItemResponse> toppingPerOrderItems;
}
