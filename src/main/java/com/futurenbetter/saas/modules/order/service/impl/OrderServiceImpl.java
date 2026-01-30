package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.MomoUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShopRepository shopRepository;
    private final OrderMapper orderMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ToppingRepository toppingRepository;
    private final OrderRepository orderRepository;
    private final MomoUtils momoUtils;
    private final ObjectMapper objectMapper;

    @Value("${momo.api-url}")
    private String momoApiUrl;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

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
        // order.setPaidPrice(totalBasePrice);
        order.setProductQuantity(items.size());

        if (request.getPaymentGateway() == PaymentGateway.CASH) {
            order.setOrderStatus(OrderStatus.PAID);
        }

        Order savedOrder = orderRepository.save(order);

        if (request.getPaymentGateway() == PaymentGateway.MOMO) {
            String orderIdMomo = "ORD_" + savedOrder.getOrderId() + "_" + System.currentTimeMillis();
            String requestId = String.valueOf(System.currentTimeMillis());
            String amountStr = String.valueOf(savedOrder.getBasePrice());
            String orderInfo = "Thanh toan don hang #" + savedOrder.getOrderId();
            String requestType = "captureWallet";

            String extraData = "";
            try {
                Map<String, Object> extraDatamap = new HashMap<>();
                extraDatamap.put("type", "ORDER");
                extraDatamap.put("orderId", savedOrder.getOrderId());

                byte[] jsonBytes = objectMapper.writeValueAsBytes(extraDatamap);
                extraData = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
            } catch (Exception e) {
                throw new BusinessException("Lỗi chuẩn bị dữ liệu thanh toán");
            }

            String finalRedirectUrl = redirectUrl + "/callback";
            String finalIpn = redirectUrl + "/ipn";

            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amountStr +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + finalIpn +
                    "&orderId=" + orderIdMomo +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + finalRedirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;
            String signature = momoUtils.hmacSha256(rawHash, secretKey);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", partnerCode);
            body.put("requestId", requestId);
            body.put("amount", savedOrder.getBasePrice());
            body.put("orderId", orderIdMomo);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", finalRedirectUrl);
            body.put("ipnUrl", finalIpn);
            body.put("requestType", requestType);
            body.put("extraData", extraData);
            body.put("signature", signature);
            body.put("lang", "vi");

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl, body, Map.class);

            if (response.getBody() != null && response.getBody().get("payUrl") != null) {
                String payUrl = (String) response.getBody().get("payUrl");
                // Gán payUrl vào OrderResponse để trả về cho Frontend
                OrderResponse orderResponse = orderMapper.toOrderResponse(savedOrder);
                orderResponse.setPayUrl(payUrl);
                return orderResponse;
            } else {
                throw new BusinessException("Lỗi kết nối cổng thanh toán MOMO");
            }
        }
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void handleMomoOrderIpn(Map<String, String> payload) {
        String momoOrderId = payload.get("orderId");
        String resultCode = payload.get("resultCode");

        if (!"0".equals(resultCode)) {
            return;
        }

        Long realOrderId;
        try {
            if (momoOrderId == null || !momoOrderId.contains("_")) {
                throw new IllegalArgumentException("Mã orderId không đúng định dạng: " + momoOrderId);
            }
            String[] parts = momoOrderId.split("_");
            realOrderId = Long.parseLong(parts[1]);
        } catch (Exception e) {
            throw new BusinessException("Định dạng orderId từ cổng thanh toán không hợp lệ");
        }

        Order order = orderRepository.findById(realOrderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + realOrderId));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            return;
        }

        order.setOrderStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                item.setOrderItemStatus(OrderItemStatus.PAID);
                item.setUpdatedAt(LocalDateTime.now());
            });
        }
        orderRepository.save(order);
    }
}
