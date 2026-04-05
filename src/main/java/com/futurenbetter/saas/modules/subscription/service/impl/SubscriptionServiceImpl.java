package com.futurenbetter.saas.modules.subscription.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.MomoUtils;
import com.futurenbetter.saas.common.utils.PayOSUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.subscription.dto.ShopSnapshot;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.VnpayPaymentResponse;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.entity.ShopSubscription;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.*;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.repository.ShopSubscriptionRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionPlanRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ShopSubscriptionRepository shopSubscriptionRepository;
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final PdfExportService pdfExportService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final MomoUtils momoUtils;
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
    public MomoPaymentResponse createSubscriptionWithMomo(SubscriptionRequest request) {
        // 1. Kiểm tra gói dịch vụ
        SubscriptionPlan plan = validateSubscriptionRequest(request);

        // 2. Tính tiền
        Long amount = calculateAmount(plan, request.getBillingCycle());

        Map<String, Object> extraDataMap = new HashMap<>();
        extraDataMap.put("shopData", request);
        extraDataMap.put("type", "SUBSCRIPTION");

        // 3. Đóng gói thông tin shop vào extra data (Base64 JSON)
        String extraData = "";
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(extraDataMap);
            extraData = Base64.getEncoder().encodeToString(jsonBytes);

        } catch (Exception e) {
            throw new BusinessException("Lỗi đóng gói thông tin shop");
        }

        // 4. Chuẩn bị tham số Momo
        String orderId = "SHOP-" + System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderInfo = "Dang ki dich vu cho shop " + request.getShopName().replaceAll("[^a-zA-Z0-9 ]", "");
        String requestType = "captureWallet";
        String amountStr = String.valueOf(amount);

        // Tạo signature
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
        String signature = momoUtils.hmacSha256(rawHash, secretKey);

        SubscriptionTransaction pendingTransaction = SubscriptionTransaction.builder()
                .orderId(orderId)
                .amount(amount)
                .plan(plan)
                .billingCycle(request.getBillingCycle())
                .paymentGateway(PaymentGatewayEnum.MOMO)
                .status(SubscriptionTransactionEnum.PENDING)
                .isIncome(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        subscriptionTransactionRepository.save(pendingTransaction);

        // 5. Gọi Momo API
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
            log.info("Sending MoMo Request (Subscription): Body={}", body);
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl, body, Map.class);
            log.info("MoMo Response (Subscription): {}", response.getBody());
            if (response.getBody() == null || response.getBody().get("payUrl") == null) {
                String resultCode = response.getBody() != null ? String.valueOf(response.getBody().get("resultCode"))
                        : "unknown";
                String message = response.getBody() != null ? String.valueOf(response.getBody().get("message"))
                        : "No message";
                log.error("MoMo Subscription Error: resultCode={}, message={}", resultCode, message);
                throw new BusinessException("Momo trả về lỗi: " + message + " (code: " + resultCode + ")");
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
        String orderId = payload.get("orderId");

        if (!"0".equals(payload.get("resultCode")))
            return;

        Optional<SubscriptionTransaction> existingTask = subscriptionTransactionRepository.findByOrderId(orderId);

        // Idempotency: Đã xử lý thành công trước đó
        if (existingTask.isPresent() && existingTask.get().getStatus() == SubscriptionTransactionEnum.ACTIVE) {
            return;
        }

        // Idempotency: shop và invoice đã có nhưng chưa ACTIVE
        if (existingTask.isPresent() && existingTask.get().getShop() != null
                && existingTask.get().getInvoice() != null) {
            SubscriptionTransaction tx = existingTask.get();
            tx.setStatus(SubscriptionTransactionEnum.ACTIVE);
            tx.setUpdatedAt(LocalDateTime.now());
            subscriptionTransactionRepository.save(tx);
            return;
        }

        // 1. Giải mã thông tin shop từ extra data
        String extraData = payload.get("extraData");
        if (extraData == null || extraData.isEmpty()) {
            throw new BusinessException("Thiếu dữ liệu extraData");
        }

        SubscriptionRequest shopData;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(extraData);
            JsonNode rootNode = objectMapper.readTree(decodedBytes);
            shopData = objectMapper.treeToValue(rootNode.get("shopData"), SubscriptionRequest.class);
        } catch (Exception e) {
            throw new BusinessException("Không thể giải mã dữ liệu shop: " + e.getMessage());
        }

        // Check trùng email/domain (chỉ nếu shop chưa được tạo)
        Shop existingShop = existingTask.isPresent() ? existingTask.get().getShop() : null;
        if (existingShop == null) {
            if (shopRepository.existsByEmail(shopData.getEmail())) {
                throw new BusinessException("Email " + shopData.getEmail() + " đã được đăng ký");
            }
            if (shopRepository.existsByDomain(shopData.getDomain())) {
                throw new BusinessException("Tên miền " + shopData.getDomain() + " đã được đăng ký");
            }
        }

        // 2. Lưu shop
        Shop shop;
        if (existingShop != null) {
            shop = existingShop;
        } else {
            shop = new Shop();
            shop.setShopName(shopData.getShopName());
            shop.setAddress(shopData.getAddress());
            shop.setPhone(shopData.getPhone());
            shop.setEmail(shopData.getEmail());
            shop.setDomain(shopData.getDomain());
            shop.setShopStatus(ShopStatus.ACTIVE);
            shop = shopRepository.save(shop);
        }

        // 3. Tạo subscription và invoice
        SubscriptionPlan plan = subscriptionPlanRepository.findById(shopData.getSubscriptionPlanId())
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        Long paidAmount = Long.parseLong(payload.get("amount"));

        ShopSubscription sub = ShopSubscription.builder()
                .shop(shop)
                .plan(plan)
                .price(paidAmount)
                .billingCycleStatus(shopData.getBillingCycle())
                .subscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE)
                .autoRenewal(shopData.getAutoRenewal())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .endedAt(
                        LocalDateTime.now().plusMonths(shopData.getBillingCycle() == BillingCycleEnum.MONTHLY ? 1 : 12))
                .build();
        shopSubscriptionRepository.save(sub);

        BillingInvoice invoice = BillingInvoice.builder()
                .shop(shop)
                .shopSubscription(sub)
                .amount(paidAmount)
                .status(InvoiceEnum.PAID)
                .dueDate(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        billingInvoiceRepository.save(invoice);

        // Export PDF (không fail nếu lỗi)
        try {
            byte[] pdfContent = pdfExportService.generateInvoicePdf(invoice);
            String fileName = "Invoice_" + invoice.getBillingInvoiceId();
            String pdfUrl = cloudinaryStorageService.uploadInvoice(pdfContent, fileName);
            invoice.setPdfUrl(pdfUrl);
            billingInvoiceRepository.save(invoice);
        } catch (Exception e) {
            log.warn("Lỗi tạo PDF invoice: {}", e.getMessage());
        }

        // 4. Cập nhật transaction
        SubscriptionTransaction transaction = existingTask.orElse(new SubscriptionTransaction());
        if (transaction.getOrderId() == null) {
            transaction.setOrderId(orderId);
        }
        transaction.setInvoice(invoice);
        transaction.setShop(shop);
        transaction.setPlan(plan);
        transaction.setBillingCycle(shopData.getBillingCycle());
        transaction.setAmount(paidAmount);
        transaction.setIsIncome(true);
        transaction.setStatus(SubscriptionTransactionEnum.ACTIVE);
        transaction.setPaymentGateway(PaymentGatewayEnum.MOMO);
        transaction.setUpdatedAt(LocalDateTime.now());
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
        subscriptionTransactionRepository.save(transaction);

        invoice.setTransaction(transaction);
        billingInvoiceRepository.save(invoice);
    }

    @Override
    public Map<String, String> queryMomoTransaction(String orderId) {
        String requestId = String.valueOf(System.currentTimeMillis());
        String requestType = "queryStatus";

        // Tạo signature theo tài liệu Momo
        String rawHash = "accessKey=" + accessKey +
                "&orderId=" + orderId +
                "&partnerCode=" + partnerCode +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        String signature = momoUtils.hmacSha256(rawHash, secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("orderId", orderId);
        body.put("signature", signature);
        body.put("lang", "vi");

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl.replace("create", "query"), body,
                    Map.class);
            return (Map<String, String>) response.getBody();
        } catch (Exception e) {
            // log.error("Lỗi truy vấn giao dịch Momo: {}", e.getMessage());
            return null;
        }
    }

    @Value("${vnpay.api-url}")
    private String vnpPayUrl;
    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;
    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;
    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Override
    @Transactional
    public VnpayPaymentResponse createSubscriptionWithVnpay(SubscriptionRequest request, String ipAddress) {
        // 1. Kiểm tra gói dịch vụ
        SubscriptionPlan plan = validateSubscriptionRequest(request);

        // 2. Tính tiền
        Long amount = calculateAmount(plan, request.getBillingCycle());

        Shop shop = new Shop();
        shop.setShopName(request.getShopName());
        shop.setAddress(request.getAddress());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setDomain(request.getDomain());
        shop.setShopStatus(ShopStatus.PENDING);
        shopRepository.save(shop);

        String orderId = "VNP_" + System.currentTimeMillis();

        SubscriptionTransaction transaction = SubscriptionTransaction.builder()
                .orderId(orderId)
                .amount(amount)
                .plan(plan)
                .shop(shop)
                .billingCycle(request.getBillingCycle())
                .paymentGateway(PaymentGatewayEnum.VNPAY)
                .status(SubscriptionTransactionEnum.PENDING)
                .isIncome(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        subscriptionTransactionRepository.save(transaction);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan goi dich vu: " + plan.getSubscriptionPlanName());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // 3. Sắp xếp tham số theo alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // 4. Xây dựng chuỗi hash và chuỗi query
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 5. Tạo Secure Hash (HMAC SHA512)
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        String paymentUrl = vnpPayUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        return VnpayPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .orderId(orderId)
                .amount(String.valueOf(amount))
                .build();
    }

    public void handleVnpayReturn(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Chỉ giữ lại các tham số bắt đầu bằng vnp_ và không phải là Hash
        Map<String, String> vnp_HashParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("vnp_") && !key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                vnp_HashParams.put(key, value);
            }
        });

        // Gọi hàm chung để tính toán lại chữ ký
        String hashData = buildHashData(vnp_HashParams);
        String reSign = hmacSHA512(vnpHashSecret, hashData);

        if (!reSign.equalsIgnoreCase(vnp_SecureHash)) {
            throw new BusinessException("Chữ ký không hợp lệ");
        }

        // 5. Kiểm tra ResponseCode
        String responseCode = params.get("vnp_ResponseCode");
        if (!"00".equals(responseCode)) {
            throw new BusinessException("Giao dịch không thành công. Mã lỗi: " + responseCode);
        }

        // 6. Logic nghiệp vụ (Khởi tạo Shop, Invoice, v.v.)
        String orderId = params.get("vnp_TxnRef");
        SubscriptionTransaction transaction = subscriptionTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch"));

        if (transaction.getStatus() == SubscriptionTransactionEnum.ACTIVE) {
            return;
        }

        Shop shop = transaction.getShop();
        shop.setShopStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        SubscriptionPlan plan = transaction.getPlan();
        BillingCycleEnum cycle = transaction.getBillingCycle();

        ShopSubscription sub = ShopSubscription.builder()
                .shop(shop)
                .plan(plan)
                .autoRenewal(true)
                .price(transaction.getAmount())
                .billingCycleStatus(cycle)
                .subscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .endedAt(LocalDateTime.now().plusMonths(1))
                .updatedAt(LocalDateTime.now())
                .build();
        shopSubscriptionRepository.save(sub);

        BillingInvoice invoice = BillingInvoice.builder()
                .shop(shop)
                .shopSubscription(sub)
                .amount(sub.getPrice())
                .status(InvoiceEnum.PAID)
                .dueDate(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        invoice.setTransaction(transaction);
        billingInvoiceRepository.save(invoice);

        BillingInvoice finalInvoice = billingInvoiceRepository.findById(invoice.getBillingInvoiceId())
                .orElseThrow(() -> new BusinessException("Lỗi nạp hóa đơn"));

        // Xuất PDF & Upload
        try {
            byte[] pdfContent = pdfExportService.generateInvoicePdf(finalInvoice);
            String fileName = "Invoice_" + invoice.getBillingInvoiceId();

            String pdfUrl = cloudinaryStorageService.uploadInvoice(pdfContent, fileName);
            invoice.setPdfUrl(pdfUrl);
            billingInvoiceRepository.save(invoice);
        } catch (Exception e) {
            log.error("Lỗi khi tạo hoặc tải lên PDF hóa đơn: {}", e.getMessage());
        }

        transaction.setInvoice(invoice);
        transaction.setStatus(SubscriptionTransactionEnum.ACTIVE);
        transaction.setUpdatedAt(LocalDateTime.now());
        subscriptionTransactionRepository.save(transaction);
        billingInvoiceRepository.flush();
    }

    // hmacSha256 removed as it is now used from MomoUtils

    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Lỗi khi tính HMAC SHA512: {}", ex.getMessage());
            return "";
        }
    }

    private String buildHashData(Map<String, String> params) {
        // 1. Sắp xếp alphabet các key
        Map<String, String> sortedMap = new TreeMap<>(params);

        // 2. Xây dựng chuỗi data
        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = sortedMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String key = entry.getKey();
            String value = entry.getValue();

            // VNPay 2.1.0: Chỉ hash các trường vnp_ và giá trị không trống
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append('=');
                // Dùng US_ASCII cho đồng bộ hoàn toàn với VNPay
                hashData.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        return hashData.toString();
    }


    @Override
    public CreatePaymentLinkResponse createSubscriptionWithPayOS(SubscriptionRequest request) {
        // 1. Kiểm tra gói dịch vụ
        SubscriptionPlan plan = validateSubscriptionRequest(request);

        // 2. Tính tiền
        Long amount = calculateAmount(plan, request.getBillingCycle());

        // 3. Serialize thông tin shop thành JSON — KHÔNG tạo Shop ở đây
        ShopSnapshot snapshot = ShopSnapshot.builder()
                .shopName(request.getShopName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .domain(request.getDomain())
                .build();

        String shopSnapshotJson;
        try {
            shopSnapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Không thể serialize thông tin shop");
        }

        // 4. Tạo transaction với shopSnapshot, không có shop
        String orderId = "PAYOS_SUB_" + System.currentTimeMillis();

        SubscriptionTransaction transaction = SubscriptionTransaction.builder()
                .orderId(orderId)
                .amount(amount)
                .plan(plan)
                .shopSnapshot(shopSnapshotJson) // <-- lưu JSON
                // .shop(shop) -- bỏ dòng này
                .billingCycle(request.getBillingCycle())
                .paymentGateway(PaymentGatewayEnum.PAYOS)
                .status(SubscriptionTransactionEnum.PENDING)
                .isIncome(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        subscriptionTransactionRepository.save(transaction);

        PaymentLinkItem paymentLinkItem = payOSService.buildPaymentLinkSubscriptionItem(transaction);
        CreatePaymentLinkResponse response = payOSService.buildPaymentLinkSubscription(transaction, paymentLinkItem);

        return response;
    }

    @Override
    public void updateSubscriptionStatus(WebhookData data) {
        Long subscriptionTransactionId = PayOSUtils.parseSubscriptionCode(data.getOrderCode());
        SubscriptionTransaction transaction = subscriptionTransactionRepository.findById(subscriptionTransactionId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch"));

        if (transaction.getStatus() == SubscriptionTransactionEnum.ACTIVE) {
            return;
        }

        // Parse shopSnapshot JSON → tạo Shop mới
        ShopSnapshot snapshot;
        try {
            snapshot = objectMapper.readValue(transaction.getShopSnapshot(), ShopSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Không thể đọc thông tin shop từ giao dịch");
        }

        Shop shop = new Shop();
        shop.setShopName(snapshot.getShopName());
        shop.setAddress(snapshot.getAddress());
        shop.setPhone(snapshot.getPhone());
        shop.setEmail(snapshot.getEmail());
        shop.setDomain(snapshot.getDomain());
        shop.setShopStatus(ShopStatus.ACTIVE); // tạo thẳng ACTIVE vì đã thanh toán
        shop = shopRepository.save(shop);

        // Phần còn lại giữ nguyên
        SubscriptionPlan plan = transaction.getPlan();
        BillingCycleEnum cycle = transaction.getBillingCycle();

        ShopSubscription sub = ShopSubscription.builder()
                .shop(shop)
                .plan(plan)
                .autoRenewal(true)
                .price(transaction.getAmount())
                .billingCycleStatus(cycle)
                .subscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .endedAt(cycle == BillingCycleEnum.MONTHLY
                        ? LocalDateTime.now().plusMonths(1)
                        : LocalDateTime.now().plusYears(1))
                .updatedAt(LocalDateTime.now())
                .build();
        shopSubscriptionRepository.save(sub);

        BillingInvoice invoice = BillingInvoice.builder()
                .shop(shop)
                .shopSubscription(sub)
                .amount(sub.getPrice())
                .status(InvoiceEnum.PAID)
                .dueDate(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        invoice.setTransaction(transaction);
        billingInvoiceRepository.save(invoice);

        BillingInvoice finalInvoice = billingInvoiceRepository.findById(invoice.getBillingInvoiceId())
                .orElseThrow(() -> new BusinessException("Lỗi nạp hóa đơn"));

        try {
            byte[] pdfContent = pdfExportService.generateInvoicePdf(finalInvoice);
            String fileName = "Invoice_" + invoice.getBillingInvoiceId();
            String pdfUrl = cloudinaryStorageService.uploadInvoice(pdfContent, fileName);
            invoice.setPdfUrl(pdfUrl);
            billingInvoiceRepository.save(invoice);
        } catch (Exception e) {
            log.error("Lỗi khi tạo hoặc tải lên PDF hóa đơn: {}", e.getMessage());
        }

        transaction.setShop(shop); // gán shop vào transaction sau khi tạo xong
        transaction.setInvoice(invoice);
        transaction.setStatus(SubscriptionTransactionEnum.ACTIVE);
        transaction.setUpdatedAt(LocalDateTime.now());
        subscriptionTransactionRepository.save(transaction);
        billingInvoiceRepository.flush();
    }

    @Override
    public boolean isPresentSubscription(long subscriptionTransactionId, long amount) {
        return subscriptionTransactionRepository.existsBySubscriptionTransactionIdAndAmount(subscriptionTransactionId, amount);
    }

    private SubscriptionPlan validateSubscriptionRequest(SubscriptionRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        if (shopRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email này đã được sử dụng để đăng kí dịch vụ");
        }

        if (shopRepository.existsByDomain(request.getDomain())) {
            throw new BusinessException("Tên miền đã được sử dụng");
        }

        return plan;
    }

    private Long calculateAmount(SubscriptionPlan plan, BillingCycleEnum billingCycle) {
        return billingCycle == BillingCycleEnum.MONTHLY ? plan.getPriceMonthly() : plan.getPriceYearly();
    }
}