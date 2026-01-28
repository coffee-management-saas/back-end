package com.futurenbetter.saas.modules.order.dto.request;

import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.enums.PaymentGateway;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    OrderType orderType;
    PaymentGateway paymentGateway;
    List<OrderItemRequest> orderItems;
    LocalDateTime createdAt;
}
