package com.futurenbetter.saas.modules.order.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AIChatOrderRequest {
    private List<OrderItemRequest> orderItems;
}
