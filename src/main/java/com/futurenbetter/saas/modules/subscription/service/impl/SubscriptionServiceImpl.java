package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
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
        String orderId = payload.get("orderId");

        if (!"0".equals(payload.get("resultCode"))) return ;

        Optional<SubscriptionTransaction> existingTask = subscriptionTransactionRepository.findByOrderId(orderId);
        if (existingTask.isPresent() && existingTask.get().getStatus() == SubscriptionTransactionEnum.ACTIVE) {
//            log.info("Giao dịch {} đã được xử lý trước đó, bỏ qua.", orderId);
            return;
        }

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

        try {
            byte[] pdfContent = pdfExportService.generateInvoicePdf(invoice);
            String fileName = "Invoice_" + invoice.getBillingInvoiceId();

            String pdfUrl = cloudinaryStorageService.uploadFile(pdfContent, fileName, "invoices");
            invoice.setPdfUrl(pdfUrl);
            billingInvoiceRepository.save(invoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //4. Lưu giao dịch
        SubscriptionTransaction transaction = existingTask.orElse(new SubscriptionTransaction());
                    transaction.setInvoice(invoice);
                    transaction.setShop(shop);
                    transaction.setPlan(plan);
                    transaction.setBillingCycle(shopData.getBillingCycle());
                    transaction.setAmount(transaction.getAmount() + sub.getPrice());
                    transaction.setIsIncome(true);
                    transaction.setStatus(SubscriptionTransactionEnum.ACTIVE);
                    transaction.setPaymentGateway(PaymentGatewayEnum.MOMO);
                    transaction.setUpdatedAt(LocalDateTime.now());
                    transaction.setCreatedAt(LocalDateTime.now());
        subscriptionTransactionRepository.save(transaction);
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
        String signature = hmacSha256(rawHash, secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("orderId", orderId);
        body.put("signature", signature);
        body.put("lang", "vi");

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiUrl.replace("create", "query"), body, Map.class);
            return (Map<String, String>) response.getBody();
        } catch (Exception e) {
//            log.error("Lỗi truy vấn giao dịch Momo: {}", e.getMessage());
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
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        Long amount = request.getBillingCycle() == BillingCycleEnum.MONTHLY
                ? plan.getPriceMonthly() : plan.getPriceYearly();

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

    private String hmacSha256(String data, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

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
}