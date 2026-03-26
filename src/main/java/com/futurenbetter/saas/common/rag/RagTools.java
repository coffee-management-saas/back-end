package com.futurenbetter.saas.common.rag;

import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.product.dto.response.ProductVariantResponse;
import com.futurenbetter.saas.modules.product.service.inter.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RagTools {

    private final ProductVariantService productVariantService;
    private final OrderService orderService;

    public static final ThreadLocal<Long> lastCreatedOrderId = new ThreadLocal<>();

    public static void clearLastCreatedOrderId() {
        lastCreatedOrderId.remove();
    }

    public static Long getLastCreatedOrderId() {
        return lastCreatedOrderId.get();
    }

    @Tool(description = "Sử dụng để lấy danh sách các món đồ uống, cà phê, topping hiện có tại quán.")
    public List<ProductVariantResponse> getMenu() {
        log.info("AI is calling getMenu tool");
        List<ProductVariantResponse> menu = productVariantService.getAllProduct();
        log.info("Found {} products in menu", menu.size());
        return menu;
    }

    @Tool(description = "Sử dụng để tạo đơn hàng sau khi đã chốt món (Tên, Size, Số lượng, Topping).")
    public String createOrder(com.futurenbetter.saas.modules.order.dto.request.AIChatOrderRequest aiRequest) {
        log.info("AI is calling createOrder tool with request: {}", aiRequest);
        try {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setOrderItems(aiRequest.getOrderItems());
            orderRequest.setCustomerId(0L);
            orderRequest.setOrderType(OrderType.ONLINE);
            orderRequest.setPaymentGateway(null);

            var response = orderService.createOrder(orderRequest);
            lastCreatedOrderId.set(response.getOrderId());
            return "Tạo đơn hàng thành công (Order ID: " + response.getOrderId()
                    + ")! Đơn hàng đang được chuẩn bị. Hãy báo lại cho khách hàng hãy thanh toán.";
        } catch (Exception e) {
            log.error("Error creating order from AI: {}", e.getMessage());
            return "Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage()
                    + ". Hãy yêu cầu khách hàng cung cấp lại thông tin. ";
        }
    }
}
