package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionTransactionResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.VnpayPaymentResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface SubscriptionService {

    //luồng thanh toán Momo
    MomoPaymentResponse createSubscriptionWithMomo(SubscriptionRequest request);
    void handleMomoIpn(Map<String, String> payload);
    Map<String, String> queryMomoTransaction(String orderId);

    //luồng thanh toán VNPay
    VnpayPaymentResponse createSubscriptionWithVnpay(SubscriptionRequest request, String ipAddress) throws UnsupportedEncodingException;
    void handleVnpayReturn(Map<String, String> params);

    // luồng thanh toán PayOS
    CreatePaymentLinkResponse createSubscriptionWithPayOS(SubscriptionRequest request);
    void updateSubscriptionStatus(WebhookData data);
    boolean isPresentSubscription(long subscriptionTransactionId, long amount);

    SubscriptionTransactionResponse getSubscriptionTransactionByOrderCode(Long orderCode);
}
