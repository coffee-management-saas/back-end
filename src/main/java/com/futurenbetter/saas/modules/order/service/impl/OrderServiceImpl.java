package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.order.dto.request.OrderItemRequest;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.dto.request.ToppingItemRequest;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.entity.OrderItem;
import com.futurenbetter.saas.modules.order.entity.ToppingPerOrderItem;
import com.futurenbetter.saas.modules.order.enums.OrderItemStatus;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.PaymentGateway;
import com.futurenbetter.saas.modules.order.mapper.OrderMapper;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import com.futurenbetter.saas.modules.product.entity.Topping;
import com.futurenbetter.saas.modules.product.repository.ProductVariantRepository;
import com.futurenbetter.saas.modules.product.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShopRepository shopRepository;
    private final OrderMapper orderMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ToppingRepository toppingRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Lấy ShopId từ SecurityUtils
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        // 2. Khởi tạo Entity Order từ Request
        Order order = orderMapper.toOrder(request);
        order.setShop(shop);

        OrderItemStatus initialItemStatus = (request.getPaymentGateway() == PaymentGateway.CASH)
                ? OrderItemStatus.PAID
                : OrderItemStatus.PENDING;

        long totalBasePrice = 0;
        List<OrderItem> items = new ArrayList<>();

        // 3. Xử lý từng Item trong đơn hàng
        for (OrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem item = orderMapper.toOrderItem(itemReq);
            item.setOrder(order);
            item.setOrderItemStatus(initialItemStatus);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            ProductVariant variant = productVariantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
            item.setUnitPrice(variant.getPrice());


            long itemTotal = variant.getPrice() * itemReq.getQuantity();

            // 4. Xử lý Toppings đi kèm
            if (itemReq.getToppingItems() != null && !itemReq.getToppingItems().isEmpty()) {
                List<ToppingPerOrderItem> toppings = new ArrayList<>();
                for (ToppingItemRequest topReq : itemReq.getToppingItems()) {
                    Topping topping = toppingRepository.findById(topReq.getToppingId())
                            .orElseThrow(() -> new BusinessException("Topping không tồn tại"));

                    ToppingPerOrderItem topEntity = orderMapper.toToppingEntity(topReq);
                    topEntity.setToppingPerOrderItemId(topping.getId());
                    topEntity.setPrice(topping.getPrice());
                    topEntity.setOrderItem(item);
                    topEntity.setTopping(topping);
                    topEntity.setCreatedAt(LocalDateTime.now());

                    itemTotal += (topping.getPrice() * topReq.getQuantity());
                    toppings.add(topEntity);
                }
                item.setToppingPerOrderItems(toppings);
            }

            totalBasePrice += itemTotal;
            items.add(item);
        }

        // 5. Cập nhật thông tin tổng quát và lưu trữ
        order.setOrderItems(items);
        order.setBasePrice(totalBasePrice);
//        order.setPaidPrice(totalBasePrice);
        order.setProductQuantity(items.size());

        if (request.getPaymentGateway() == PaymentGateway.CASH) {
            order.setOrderStatus(OrderStatus.PAID);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }
}
