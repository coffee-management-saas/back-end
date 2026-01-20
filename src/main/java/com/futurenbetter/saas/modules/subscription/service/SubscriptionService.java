package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.VnpayPaymentResponse;

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
}
