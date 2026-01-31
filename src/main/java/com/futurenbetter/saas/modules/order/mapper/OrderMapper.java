package com.futurenbetter.saas.modules.order.mapper;

import com.futurenbetter.saas.modules.order.dto.request.OrderItemRequest;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.dto.request.ToppingItemRequest;
import com.futurenbetter.saas.modules.order.dto.response.OrderItemResponse;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.dto.response.ToppingPerOrderItemResponse;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.entity.OrderItem;
import com.futurenbetter.saas.modules.order.entity.ToppingPerOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "orderStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    Order toOrder(OrderRequest orderRequest);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "orderItemStatus", ignore = true)
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);

    @Mapping(target = "toppingPerOrderItemId", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "price", ignore = true)
    ToppingPerOrderItem toToppingEntity(ToppingItemRequest toppingItemRequest);

    OrderResponse toOrderResponse(Order order);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "toppingPerOrderItemId", source = "toppingPerOrderItemId")
    @Mapping(target = "toppingName", source = "topping.name")
    ToppingPerOrderItemResponse toToppingResponse(ToppingPerOrderItem topping);
}
