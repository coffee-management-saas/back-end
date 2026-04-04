package com.futurenbetter.saas.modules.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.MomoUtils;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import com.futurenbetter.saas.modules.auth.entity.PointHistory;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.PointHistoryEnum;
import com.futurenbetter.saas.modules.auth.mapper.PointHistoryMapper;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.MembershipRankRepository;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.MonthlyProductSoldService;
import com.futurenbetter.saas.modules.inventory.service.inter.InventoryInvoiceService;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import com.futurenbetter.saas.modules.order.dto.filter.OrderFilter;
import com.futurenbetter.saas.modules.order.dto.request.OrderItemRequest;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;
import com.futurenbetter.saas.modules.order.dto.request.ToppingItemRequest;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.entity.OrderItem;
import com.futurenbetter.saas.modules.order.entity.ToppingPerOrderItem;
import com.futurenbetter.saas.modules.order.enums.OrderItemStatus;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import com.futurenbetter.saas.modules.order.enums.PaymentGateway;
import com.futurenbetter.saas.modules.order.mapper.OrderMapper;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.order.repository.PointHistoryRepository;
import com.futurenbetter.saas.modules.order.service.GoongMapService;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.order.specification.OrderSpecification;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import com.futurenbetter.saas.modules.product.entity.Topping;
import com.futurenbetter.saas.modules.product.repository.ProductVariantRepository;
import com.futurenbetter.saas.modules.product.repository.ToppingRepository;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.DiscountTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import com.futurenbetter.saas.modules.promotion.service.PromotionService;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShopRepository shopRepository;
    private final OrderMapper orderMapper;
    private final ProductVariantRepository productVariantRepository;
    private final ToppingRepository toppingRepository;
    private final OrderRepository orderRepository;
    private final InventoryInvoiceService inventoryInvoiceService;
    private final MomoUtils momoUtils;
    private final ObjectMapper objectMapper;
    private final PromotionService promotionService;
    private final PdfExportService pdfExportService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final CustomerRepository customerRepository;
    private final MembershipRankRepository membershipRankRepository;
    private final PointHistoryMapper pointHistoryMapper;
    private final PointHistoryRepository pointHistoryRepository;
    private final NotificationService notificationService;
    private final MonthlyProductSoldService monthlyProductSoldService;
    private final AsyncOrderTaskServiceImpl asyncOrderTaskService;
    private final GoongMapService goongMapService;
    private final GoogleMapService googleMapService;
    private final PayOSService payOSService;

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
        // 1. Lấy ShopId và Customer từ SecurityUtils
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<String> roles = SecurityUtils.getCurrentUserRoles();
        Customer currentCustomer = SecurityUtils.getCurrentCustomer();

        boolean isCustomer = roles.contains("ROLE_CUSTOMER") || roles.contains("CUSTOMER");
        boolean isStaff = roles.contains("ROLE_EMPLOYEE") || roles.contains("EMPLOYEE");

        if (currentShopId == null) {
            throw new BusinessException("Không tìm thấy thông tin cửa hàng. Vui lòng đăng nhập lại.");
        }

        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        // 2. Khởi tạo Entity Order từ Request
        Order order = orderMapper.toOrder(request);
        order.setShop(shop);

        Customer targetCustomer = null;
        Long targetCustomerId = null;

        if (isCustomer) {
            targetCustomer = currentCustomer;
            order.setCustomer(targetCustomer);
        } else if (isStaff) {
            if (request.getCustomerId() != null) {
                targetCustomer = customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new BusinessException("Khách hàng không tồn tại"));
                targetCustomerId = currentUserId;
                order.setCustomer(targetCustomer);
            } else {
                order.setCustomer(null);
            }
            // set Employee id
        } else {
            throw new BusinessException("Bạn không có quyền tạo đơn hàng này");
        }

        boolean isCash = request.getPaymentGateway() == PaymentGateway.CASH;
        OrderItemStatus initialItemStatus = isCash ? OrderItemStatus.PAID : OrderItemStatus.PENDING;

        long totalBasePrice = 0;
        List<OrderItem> items = new ArrayList<>();

        // 3. Xử lý từng Item trong đơn hàng
        for (OrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem item = orderMapper.toOrderItem(itemReq);
            item.setOrder(order);
            item.setOrderItemStatus(initialItemStatus);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            if (itemReq.getProductVariantId() == null) {
                throw new BusinessException("Mã sản phẩm không được để trống");
            }

            ProductVariant variant = productVariantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
            item.setProductVariant(variant);
            item.setUnitPrice(variant.getPrice());

            long itemTotal = (variant.getPrice() * itemReq.getQuantity());

            // 4. Xử lý Toppings đi kèm
            if (itemReq.getToppingItems() != null && !itemReq.getToppingItems().isEmpty()) {
                List<ToppingPerOrderItem> toppings = new ArrayList<>();
                for (ToppingItemRequest topReq : itemReq.getToppingItems()) {
                    if (topReq.getToppingId() == null) {
                        throw new BusinessException("Mã topping không được để trống");
                    }

                    Topping topping = toppingRepository.findById(topReq.getToppingId())
                            .orElseThrow(() -> new BusinessException("Topping không tồn tại"));

                    ToppingPerOrderItem topEntity = orderMapper.toToppingEntity(topReq);
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

        // 5. Áp dụng promotion (nếu có)
        Long discountAmount = 0L;
        Promotion promotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()) {
            promotion = promotionService.validatePromotion(
                    request.getPromotionCode(),
                    currentShopId,
                    currentUserId,
                    totalBasePrice);

            if (promotion.getPromotionType() == PromotionTypeEnum.ORDER) {
                discountAmount = calculateDiscount(promotion, totalBasePrice);
            } else if (promotion.getPromotionType() == PromotionTypeEnum.PRODUCT) {
                if (promotion.getPromotionTargets() == null || promotion.getPromotionTargets().isEmpty()) {
                    throw new BusinessException("Mã khuyến mãi không có sản phẩm áp dụng");
                }

                Set<Long> targetProductIds = promotion.getPromotionTargets().stream()
                        .map(target -> target.getProduct().getId())
                        .collect(Collectors.toSet());

                long eligibleAmount = 0L;
                for (OrderItem item : items) {
                    Long productId = item.getProductVariant().getProduct().getId();

                    if (targetProductIds.contains(productId)) {
                        long itemPrice = item.getUnitPrice() * item.getQuantity();
                        if (item.getToppingPerOrderItems() != null) {
                            for (ToppingPerOrderItem topping : item.getToppingPerOrderItems()) {
                                itemPrice += topping.getPrice() * topping.getQuantity();
                            }
                        }
                        eligibleAmount += itemPrice;
                    }
                }

                if (eligibleAmount == 0) {
                    throw new BusinessException("Đơn hàng không có sản phẩm áp dụng mã khuyến mãi này");
                }

                discountAmount = calculateDiscount(promotion, eligibleAmount);
            }
        }

        // 6. Cập nhật thông tin tổng quát và lưu trữ
        order.setOrderItems(items);
        order.setBasePrice(totalBasePrice);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setLatitude(request.getLatitude());
        order.setLongitude(request.getLongitude());
        order.setProductQuantity(items.size());
        order.setPromotion(promotion);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal shippingFee = BigDecimal.ZERO;

        if (request.getOrderType() == OrderType.DELIVERY && request.getLatitude() != null) {
            Double shopLat = 10.7725;
            Double shopLng = 106.6981;

            String origin = shopLat + "," + shopLng;
            String destination = request.getLatitude() + "," + request.getLongitude();
            double distanceKm = goongMapService.getDistance(origin, destination);

            if (distanceKm > 1.0) {
                double extraKm = Math.ceil(distanceKm - 1.0);
                shippingFee = BigDecimal.valueOf(extraKm * 5000);
            }
        }

        order.setShippingFee(shippingFee);
        long paidPrice = totalBasePrice - discountAmount + shippingFee.longValue();
        order.setPaidPrice(paidPrice);

        if (isCash) {
            order.setOrderStatus(OrderStatus.PAID);
            updateMonthlyProductSold(order);
        } else {
            order.setOrderStatus(OrderStatus.PENDING);
        }

        Order savedOrder = orderRepository.save(order);

        // 7. Ghi nhận promotion usage và xử lý nội bộ nếu đơn hàng đã được THANH TOÁN
        // (thường là Offline Cash)
        if (savedOrder.getOrderStatus() == OrderStatus.PAID) {
            triggerStockDeduction(savedOrder);
            processCustomerPoints(savedOrder);

            if (promotion != null) {

                promotionService.recordPromotionUsage(
                        promotion.getPromotionId(),
                        currentUserId,
                        currentShopId,
                        discountAmount);
            }
            asyncOrderTaskService.generateAndUploadInvoice(savedOrder.getOrderId());

        }

        if (request.getPaymentGateway() == PaymentGateway.MOMO) {
            String payUrl = createMomoPayment(savedOrder, redirectUrl);
            OrderResponse orderResponse = orderMapper.toOrderResponse(savedOrder);
            orderResponse.setPayUrl(payUrl);
            return orderResponse;
        }
        return orderMapper.toOrderResponse(savedOrder);
    }

    private String createMomoPayment(Order order, String specificRedirectUrl) {
        String effectiveRedirectUrl = (specificRedirectUrl != null && !specificRedirectUrl.isEmpty())
                ? specificRedirectUrl
                : redirectUrl;

        String orderIdMomo = "ORD_" + order.getOrderId() + "_" + System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderInfo = "Thanh toan don hang " + order.getOrderId();
        String requestType = "captureWallet";

        String extraData = "";
        try {
            Map<String, Object> extraDatamap = new HashMap<>();
            extraDatamap.put("type", "ORDER");
            extraDatamap.put("orderId", order.getOrderId());

            byte[] jsonBytes = objectMapper.writeValueAsBytes(extraDatamap);
            extraData = Base64.getEncoder().encodeToString(jsonBytes);
        } catch (Exception e) {
            throw new BusinessException("Lỗi chuẩn bị dữ liệu thanh toán");
        }

        String paidAmountStr = String.valueOf(order.getPaidPrice());
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + paidAmountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderIdMomo +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + effectiveRedirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        String signature = momoUtils.hmacSha256(rawHash, secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("amount", order.getPaidPrice());
        body.put("orderId", orderIdMomo);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", effectiveRedirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("requestType", requestType);
        body.put("extraData", extraData);
        body.put("signature", signature);
        body.put("lang", "vi");

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl, body, Map.class);

        if (response.getBody() != null && response.getBody().get("payUrl") != null) {
            return (String) response.getBody().get("payUrl");
        } else {
            String resultCode = response.getBody() != null ? String.valueOf(response.getBody().get("resultCode"))
                    : "unknown";
            String message = response.getBody() != null ? String.valueOf(response.getBody().get("message"))
                    : "No message";
            throw new BusinessException(
                    "Lỗi kết nối cổng thanh toán MOMO: " + message + " (code: " + resultCode + ")");
        }
    }

    @Override
    @Transactional
    public OrderResponse initiatePayment(Long orderId, String returnUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new BusinessException("Đơn hàng này đã được thanh toán");
        }

        order.setPaymentGateway(PaymentGateway.MOMO);
        orderRepository.save(order);

        String payUrl = createMomoPayment(order, returnUrl);
        OrderResponse response = orderMapper.toOrderResponse(order);
        response.setPayUrl(payUrl);
        return response;
    }

    @Override
    @Transactional
    public OrderResponse confirmCashPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new BusinessException("Đơn hàng này đã được thanh toán");
        }

        order.setOrderStatus(OrderStatus.PAID);
        order.setPaymentGateway(PaymentGateway.CASH);
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                item.setOrderItemStatus(OrderItemStatus.PAID);
                item.setUpdatedAt(LocalDateTime.now());
            });
        }

        Order savedOrder = orderRepository.save(order);

        triggerStockDeduction(savedOrder);
        updateMonthlyProductSold(savedOrder);
        processCustomerPoints(savedOrder);
        asyncOrderTaskService.generateAndUploadInvoice(savedOrder.getOrderId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void handleMomoOrderIpn(Map<String, String> payload) {
        log.info("[MOMO IPN] Received payload: {}", payload);
        String momoOrderId = payload.get("orderId");
        String resultCode = payload.get("resultCode");
        String extraData = payload.get("extraData");

        if (!"0".equals(resultCode)) {
            return;
        }

        Long realOrderId = null;

        if (extraData != null && !extraData.isEmpty()) {
            try {
                Map<String, Object> data = momoUtils.decodeExtraData(extraData);
                if (data != null && "ORDER".equals(data.get("type"))) {
                    Object oid = data.get("orderId");
                    if (oid != null) {
                        realOrderId = Long.parseLong(String.valueOf(oid));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (realOrderId == null && momoOrderId != null && momoOrderId.contains("_")) {
            try {
                String[] parts = momoOrderId.split("_");
                if (parts.length > 1) {
                    realOrderId = Long.parseLong(parts[1]);
                }
            } catch (Exception e) {
            }
        }

        if (realOrderId == null) {
            return;
        }

        // 2. Load order and update status
        Long finalRealOrderId = realOrderId;
        Order order = orderRepository.findById(realOrderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + finalRealOrderId));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            log.info("[MOMO IPN] Order {} already PAID, skipping.", realOrderId);
            return;
        }

        order.setOrderStatus(OrderStatus.PAID);
        order.setPaymentGateway(PaymentGateway.MOMO);
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                item.setOrderItemStatus(OrderItemStatus.PAID);
                item.setUpdatedAt(LocalDateTime.now());
            });
        }

        order = orderRepository.save(order);
        log.info("[MOMO IPN] Order {} successfully updated to PAID.", realOrderId);

        // 3. Secondary tasks (non-blocking for the payment status)
        try {
            triggerStockDeduction(order);
        } catch (Exception e) {
            log.error("[MOMO IPN] Stock deduction failed for order {}: {}", realOrderId, e.getMessage());
        }

        try {
            updateMonthlyProductSold(order);
        } catch (Exception e) {
            log.error("[MOMO IPN] Monthly product sold update failed for order {}: {}", realOrderId, e.getMessage());
        }

        try {
            if (order.getPromotion() != null) {
                Long customerId = (order.getCustomer() != null) ? order.getCustomer().getId() : null;
                promotionService.recordPromotionUsage(
                        order.getPromotion().getPromotionId(),
                        customerId,
                        order.getShop().getId(),
                        order.getDiscountAmount());
            }
        } catch (Exception e) {
            log.error("[MOMO IPN] Promotion usage recording failed for order {}: {}", realOrderId, e.getMessage());
        }

        try {
            processCustomerPoints(order);
        } catch (Exception e) {
            log.error("[MOMO IPN] Customer points processing failed for order {}: {}", realOrderId, e.getMessage());
        }

        try {
            asyncOrderTaskService.generateAndUploadInvoice(order.getOrderId());
        } catch (Exception e) {
            log.error("[MOMO IPN] Invoice generation failed for order {}: {}", realOrderId, e.getMessage());
        }

    }

    private Long calculateDiscount(Promotion promotion, Long baseAmount) {
        Long discountAmount = 0L;

        if (promotion.getDiscountType() == DiscountTypeEnum.PERCENTAGE) {
            discountAmount = (long) (baseAmount * promotion.getDiscountValue() / 100);

            if (promotion.getMaxDiscountAmount() != null && promotion.getMaxDiscountAmount() > 0) {
                long maxDiscount = promotion.getMaxDiscountAmount();
                if (discountAmount > maxDiscount) {
                    discountAmount = maxDiscount;
                }
            }
        } else if (promotion.getDiscountType() == DiscountTypeEnum.FIXED_AMOUNT) {
            discountAmount = promotion.getDiscountValue().longValue();
            if (discountAmount > baseAmount) {
                discountAmount = baseAmount;
            }
        }
        return discountAmount;
    }

    private void triggerStockDeduction(Order order) {
        Long shopId = order.getShop().getId();

        for (OrderItem item : order.getOrderItems()) {
            // 1. Trừ kho sản phẩm chính
            inventoryInvoiceService.deductStock(
                    shopId,
                    item.getProductVariant().getId(),
                    null,
                    Double.valueOf(item.getQuantity()),
                    order.getOrderId());

            // 2. Trừ kho các topping đi kèm (nếu có)
            if (item.getToppingPerOrderItems() != null) {
                for (ToppingPerOrderItem topping : item.getToppingPerOrderItems()) {
                    inventoryInvoiceService.deductStock(
                            shopId,
                            null,
                            topping.getTopping().getId(),
                            (double) topping.getQuantity(),
                            order.getOrderId());
                }
            }
        }
    }

    private void processCustomerPoints(Order order) {
        if (order.getCustomer() == null)
            return;

        // Re-fetch customer to ensure attached state and access to lazy fields
        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElse(null);
        if (customer == null)
            return;

        MembershipRank currentRank = customer.getMembershipRank();
        if (currentRank == null || currentRank.getPointRate() == null)
            return;

        int earnedPoints = (int) Math.floor(order.getPaidPrice() * currentRank.getPointRate());
        if (earnedPoints <= 0)
            return;

        int beforePoints = (customer.getTotalPoint() != null) ? customer.getTotalPoint().intValue() : 0;
        int afterPoints = beforePoints + earnedPoints;

        customer.setTotalPoint((double) afterPoints);

        PointHistory history = PointHistory.builder()
                .customer(customer)
                .beforePoints(beforePoints)
                .pointChange(earnedPoints)
                .afterPoints(afterPoints)
                .pointHistoryStatus(PointHistoryEnum.EARNED)
                .order(order)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepository.save(history);
        updateMemberShipRank(customer, afterPoints, order.getShop().getId());
        customerRepository.save(customer);
    }

    private void updateMemberShipRank(Customer customer, int totalPoints, Long shopId) {
        List<MembershipRank> ranks = membershipRankRepository.findByShopIdOrderByRequiredPointsDesc(shopId);

        for (MembershipRank rank : ranks) {
            if (totalPoints >= rank.getRequiredPoints()) {
                if (!rank.getId().equals(customer.getMembershipRank().getId())) {
                    customer.setMembershipRank(rank);
                }
                break;
            }
        }
    }

    @Override
    public Page<OrderResponse> getOrderHistory(OrderFilter filter) {
        Long currentShopId = SecurityUtils.getCurrentShopId();

        if (SecurityUtils.isCustomer()) {
            Long currentCustomerId = SecurityUtils.getCurrentUserId();
            filter.setCustomerId(currentCustomerId);
        } else if (SecurityUtils.isUserProfile()) {
            // có thể thêm logic để nhân vie ktra dơn trong ca của họ
        }

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.filter(currentShopId, filter),
                filter.getPageable());

        return orderPage.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Security check: must belong to current shop
        if (!order.getShop().getId().equals(currentShopId)) {
            throw new BusinessException("Đơn hàng không thuộc cửa hàng này");
        }

        // Optional: If customer, must belong to them
        if (SecurityUtils.isCustomer()) {
            Long currentCustomerId = SecurityUtils.getCurrentUserId();
            if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentCustomerId)) {
                throw new BusinessException("Bạn không có quyền xem đơn hàng này");
            }
        }

        return orderMapper.toOrderResponse(order);
    }

    private void updateMonthlyProductSold(Order order) {

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return;
        }

        for (OrderItem item : order.getOrderItems()) {
            monthlyProductSoldService.updateMonthlyProductSold(
                    order.getShop(),
                    item.getProductVariant().getProduct(),
                    item.getQuantity());
        }
    }


    @Override
    @Transactional
    public CreatePaymentLinkResponse createOrderv2(OrderRequest request) {
        // 1. Lấy ShopId và Customer từ SecurityUtils
        Long currentShopId = SecurityUtils.getCurrentShopId();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<String> roles = SecurityUtils.getCurrentUserRoles();
        Customer currentCustomer = SecurityUtils.getCurrentCustomer();

        boolean isCustomer = roles.contains("ROLE_CUSTOMER") || roles.contains("CUSTOMER");
        boolean isStaff = roles.contains("ROLE_EMPLOYEE") || roles.contains("EMPLOYEE");

        if (currentShopId == null) {
            throw new BusinessException("Không tìm thấy thông tin cửa hàng. Vui lòng đăng nhập lại.");
        }

        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        // 2. Khởi tạo Entity Order từ Request
        Order order = orderMapper.toOrder(request);
        order.setShop(shop);

        Customer targetCustomer = null;
        Long targetCustomerId = null;

        if (isCustomer) {
            targetCustomer = currentCustomer;
            order.setCustomer(targetCustomer);
        } else if (isStaff) {
            if (request.getCustomerId() != null) {
                targetCustomer = customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new BusinessException("Khách hàng không tồn tại"));
                targetCustomerId = currentUserId;
                order.setCustomer(targetCustomer);
            } else {
                order.setCustomer(null);
            }
            // set Employee id
        } else {
            throw new BusinessException("Bạn không có quyền tạo đơn hàng này");
        }

        // Thanh toán tiền mặt (OFFLINE hoặc ONLINE): hoàn thành ngay theo yêu cầu
        boolean isCash = request.getPaymentGateway() == PaymentGateway.CASH;
        OrderItemStatus initialItemStatus = isCash ? OrderItemStatus.PAID : OrderItemStatus.PENDING;

        long totalBasePrice = 0;
        List<OrderItem> items = new ArrayList<>();

        // 3. Xử lý từng Item trong đơn hàng
        for (OrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem item = orderMapper.toOrderItem(itemReq);
            item.setOrder(order);
            item.setOrderItemStatus(initialItemStatus);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            if (itemReq.getProductVariantId() == null) {
                throw new BusinessException("Mã sản phẩm không được để trống");
            }

            ProductVariant variant = productVariantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new BusinessException("Sản phẩm không tồn tại"));
            item.setProductVariant(variant);
            item.setUnitPrice(variant.getPrice());

            long itemTotal = (variant.getPrice() * itemReq.getQuantity());

            // 4. Xử lý Toppings đi kèm
            if (itemReq.getToppingItems() != null && !itemReq.getToppingItems().isEmpty()) {
                List<ToppingPerOrderItem> toppings = new ArrayList<>();
                for (ToppingItemRequest topReq : itemReq.getToppingItems()) {
                    if (topReq.getToppingId() == null) {
                        throw new BusinessException("Mã topping không được để trống");
                    }

                    Topping topping = toppingRepository.findById(topReq.getToppingId())
                            .orElseThrow(() -> new BusinessException("Topping không tồn tại"));

                    ToppingPerOrderItem topEntity = orderMapper.toToppingEntity(topReq);
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

        // 5. Áp dụng promotion (nếu có)
        Long discountAmount = 0L;
        Promotion promotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()) {
            promotion = promotionService.validatePromotion(
                    request.getPromotionCode(),
                    currentShopId,
                    currentUserId,
                    totalBasePrice);

            if (promotion.getPromotionType() == PromotionTypeEnum.ORDER) {
                discountAmount = calculateDiscount(promotion, totalBasePrice);
            } else if (promotion.getPromotionType() == PromotionTypeEnum.PRODUCT) {
                if (promotion.getPromotionTargets() == null || promotion.getPromotionTargets().isEmpty()) {
                    throw new BusinessException("Mã khuyến mãi không có sản phẩm áp dụng");
                }

                Set<Long> targetProductIds = promotion.getPromotionTargets().stream()
                        .map(target -> target.getProduct().getId())
                        .collect(Collectors.toSet());

                long eligibleAmount = 0L;
                for (OrderItem item : items) {
                    Long productId = item.getProductVariant().getProduct().getId();

                    if (targetProductIds.contains(productId)) {
                        long itemPrice = item.getUnitPrice() * item.getQuantity();
                        if (item.getToppingPerOrderItems() != null) {
                            for (ToppingPerOrderItem topping : item.getToppingPerOrderItems()) {
                                itemPrice += topping.getPrice() * topping.getQuantity();
                            }
                        }
                        eligibleAmount += itemPrice;
                    }
                }

                if (eligibleAmount == 0) {
                    throw new BusinessException("Đơn hàng không có sản phẩm áp dụng mã khuyến mãi này");
                }

                discountAmount = calculateDiscount(promotion, eligibleAmount);
            }
        }

        // 6. Cập nhật thông tin tổng quát và lưu trữ
        order.setOrderItems(items);
        order.setBasePrice(totalBasePrice);
        order.setDiscountAmount(discountAmount);
        order.setPaidPrice(totalBasePrice - discountAmount);
        order.setProductQuantity(items.size());
        order.setPromotion(promotion);
        order.setCreatedAt(LocalDateTime.now());

        if (isCash) {
            order.setOrderStatus(OrderStatus.PAID);
            updateMonthlyProductSold(order);
        } else {
            order.setOrderStatus(OrderStatus.PENDING);
        }

        Order savedOrder = orderRepository.save(order);

        // 7. Ghi nhận promotion usage và xử lý nội bộ nếu đơn hàng đã được THANH TOÁN
        // (thường là Offline Cash)
        if (savedOrder.getOrderStatus() == OrderStatus.PAID) {
            triggerStockDeduction(savedOrder);
            processCustomerPoints(savedOrder);

            if (promotion != null) {

                promotionService.recordPromotionUsage(
                        promotion.getPromotionId(),
                        currentUserId,
                        currentShopId,
                        discountAmount);
            }
            asyncOrderTaskService.generateAndUploadInvoice(savedOrder.getOrderId());

        }

        if (request.getPaymentGateway() == PaymentGateway.PAYOS) {
            PaymentLinkItem paymentLinkItem = payOSService.buildPaymentLinkItem(savedOrder);
            CreatePaymentLinkResponse data = payOSService.buildPaymentLink(savedOrder, paymentLinkItem);
            OrderResponse orderResponse = orderMapper.toOrderResponse(savedOrder);
            orderResponse.setPayUrl(data.getCheckoutUrl());
            return data;
        }
        return null;
    }

    @Override
    public void updateOrderStatus(WebhookData webhookData) {
        Order order = orderRepository.findById(webhookData.getOrderCode())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + webhookData.getOrderCode()));

        if (webhookData.getCode().equals("00")) {
            order.setOrderStatus(OrderStatus.PAID);
            order.setPaymentGateway(PaymentGateway.PAYOS);
            order.setUpdatedAt(LocalDateTime.now());

            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(item -> {
                    item.setOrderItemStatus(OrderItemStatus.PAID);
                    item.setUpdatedAt(LocalDateTime.now());
                });
            }

            order = orderRepository.save(order);

            updateMonthlyProductSold(order);
            triggerStockDeduction(order);
            processCustomerPoints(order);
            asyncOrderTaskService.generateAndUploadInvoice(order.getOrderId());

        }
    }
}