package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;

import java.awt.*;
import java.util.Map;

public interface SubscriptionService {

    //luồng thanh toán
    MomoPaymentResponse createSubscriptionWithMomo(SubscriptionRequest request);
    void handleMomoIpn(Map<String, String> payload);
    Map<String, String> queryMomoTransaction(String orderId);
}
