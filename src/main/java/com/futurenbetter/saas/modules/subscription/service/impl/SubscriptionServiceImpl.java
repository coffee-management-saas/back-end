package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.entity.ShopSubscription;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.*;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.repository.ShopSubscriptionRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionPlanRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ShopSubscriptionRepository shopSubscriptionRepository;
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;

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
    public MomoPaymentResponse createSubscriptionWithMomo(SubscriptionRequest request) {
        //1. Kiểm tra gói dịch vụ
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        if (shopRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email này đã được sử dụng để đăng kí dịch vụ");
        }

        if (shopRepository.existsByDomain(request.getDomain())) {
            throw new BusinessException("Tên miền đã được sử dụng");
        }

        //2. Tính tiền
        Long amount = request.getBillingCycle() == BillingCycleEnum.MONTHLY
                ? plan.getPriceMonthly() : plan.getPriceYearly();

        //3. Đóng gói thông tin shop vào extra data (Base64 JSON)
        String extraData = "";
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(request);
            extraData = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);

        } catch (Exception e) {
            throw new BusinessException("Lỗi đóng gói thông tin shop");
        }

        //4. Chuẩn bị tham số Momo
        String orderId = "NEW_SHOP_" + System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderInfo = "Dang ki dich vu cho shop" + request.getShopName();
        String requestType =  "captureWallet";
        String amountStr = String.valueOf(amount);

        //Tạo signature
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        String signature = hmacSha256(rawHash, secretKey);

        //5. Gọi Momo API
        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("requestType", requestType);
        body.put("extraData", extraData);
        body.put("signature", signature);
        body.put("lang", "vi");

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl, body, Map.class);
            if (response.getBody() == null || response.getBody().get("payUrl") == null) {
                throw new BusinessException("Momo trả về lỗi: " + response.getBody().get("message"));
            }
            String payUrl = (String) response.getBody().get("payUrl");

            return MomoPaymentResponse.builder()
                    .payUrl(payUrl)
                    .orderId(orderId)
                    .amount(String.valueOf(amount))
                    .build();
        } catch (Exception e) {
            throw new BusinessException("Không thể kết nối với cổng thanh toán Momo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleMomoIpn(Map<String, String> payload) {
        if (!"0".equals(payload.get("resultCode"))) return ;

        //1. Giải mã thông tin shop từ extra data
        String extraData = payload.get("extraData");
        if (extraData == null || extraData.isEmpty()) {
            throw new BusinessException("Thiếu dữ liệu extraData");
        }
        SubscriptionRequest shopData;
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(extraData);
            shopData = objectMapper.readValue(decodedBytes, SubscriptionRequest.class);
        } catch (Exception e) {
            throw new BusinessException("Không thể giải mã dữ liệu shop");
        }

        if (shopRepository.existsByEmail(shopData.getEmail()) ||
                shopRepository.existsByDomain(shopData.getDomain())) {
            throw new BusinessException("Email hoặc tên miền đã được đăng ký trong lúc chờ thanh toán");
        }

        //2. Lưu shop vào database
        Shop shop = new Shop();
        shop.setShopName(shopData.getShopName());
        shop.setAddress(shopData.getAddress());
        shop.setPhone(shopData.getPhone());
        shop.setEmail(shopData.getEmail());
        shop.setDomain(shopData.getDomain());
        shop.setShopStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        //3. Tạo subscription và invoice với shopId
        SubscriptionPlan plan = subscriptionPlanRepository.findById(shopData.getSubscriptionPlanId()).get();

        ShopSubscription sub = ShopSubscription.builder()
                .shop(shop)
                .plan(plan)
                .price(Long.parseLong(payload.get("amount")))
                .billingCycleStatus(shopData.getBillingCycle())
                .subscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE)
                .autoRenewal(shopData.getAutoRenewal())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .endedAt(LocalDateTime.now().plusMonths(shopData.getBillingCycle()== BillingCycleEnum.MONTHLY ? 1 : 12))
                .build();
        shopSubscriptionRepository.save(sub);

        BillingInvoice invoice = BillingInvoice.builder()
                .shop(shop)
                .shopSubscription(sub)
                .amount(sub.getPrice())
                .status(InvoiceEnum.PAID)
                .dueDate(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();
        billingInvoiceRepository.save(invoice);

        //4. Lưu giao dịch
        SubscriptionTransaction transaction = SubscriptionTransaction.builder()
                .invoice(invoice)
                .amount(invoice.getAmount())
                .paymentGateway(PaymentGatewayEnum.MOMO)
                .status(SubscriptionTransactionEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        subscriptionTransactionRepository.save(transaction);
    }

    private String hmacSha256(String data, String key) {
        try {
            byte[] keyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo signature: " + e.getMessage());
        }
    }
}