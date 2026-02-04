package com.futurenbetter.saas.modules.order.dto.response;

import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.enums.PaymentGateway;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long orderId;
    Long basePrice;
    Long paidPrice;
    int productQuantity;
    OrderType orderType;
    PaymentGateway paymentGateway;
    OrderStatus orderStatus;
    LocalDateTime createdAt;
    List<OrderItemResponse> orderItems;
    String payUrl;
}
