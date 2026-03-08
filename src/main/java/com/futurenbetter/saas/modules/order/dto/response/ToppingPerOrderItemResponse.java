package com.futurenbetter.saas.modules.order.dto.response;

import com.futurenbetter.saas.modules.order.enums.ToppingPerOrderItemStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingPerOrderItemResponse {

    Long toppingPerOrderItemId;
    int quantity;
    Long price;
    ToppingPerOrderItemStatus status;
    String toppingName;
}
