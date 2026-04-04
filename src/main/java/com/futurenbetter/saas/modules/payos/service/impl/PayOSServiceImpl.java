package com.futurenbetter.saas.modules.payos.service.impl;

import com.futurenbetter.saas.common.utils.PayOSUtils;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${payos.order.return-url}")
    private String orderReturnUrl;

    @Value("${payos.order.cancel-url}")
    private String orderCancelUrl;

    @Value("${payos.subscription.return-url}")
    private String subscriptionReturnUrl;

    @Value("${payos.subscription.cancel-url}")
    private String subscriptionCancelUrl;



    @Override
    public PaymentLinkItem buildPaymentLinkOrderItem(Order order) {
        return PaymentLinkItem.builder()
                .name("ORDER: " + order.getOrderId())
                .quantity(1)
                .price(order.getPaidPrice())
                .build();
    }

    @Override
    public CreatePaymentLinkResponse buildPaymentLinkOrder(Order order, PaymentLinkItem paymentLinkItem) {
        Long orderCode = PayOSUtils.genOrderCodeV2(order.getShop().getId(), order.getOrderId());
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .description("ORDER: " + order.getOrderId())
                        .amount(order.getPaidPrice())
                        .item(paymentLinkItem)
                        .returnUrl(orderReturnUrl)
                        .cancelUrl(orderCancelUrl)
                        .build();

        return payOS.paymentRequests().create(paymentData);
    }

    @Override
    public PaymentLinkItem buildPaymentLinkSubscriptionItem(SubscriptionTransaction subscriptionTransaction) {
        return PaymentLinkItem.builder()
                .name("SUB_PLAN: " + subscriptionTransaction.getShop().getId())
                .quantity(1)
                .price(subscriptionTransaction.getAmount())
                .build();
    }

    @Override
    public CreatePaymentLinkResponse buildPaymentLinkSubscription(SubscriptionTransaction subscriptionTransaction, PaymentLinkItem paymentLinkItem) {
        Long orderCode = PayOSUtils.genSubscriptionCode(subscriptionTransaction.getSubscriptionTransactionId());
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .description("SUB_PLAN: " + subscriptionTransaction.getShop().getId())
                        .amount(subscriptionTransaction.getAmount())
                        .item(paymentLinkItem)
                        .returnUrl(subscriptionReturnUrl)
                        .cancelUrl(subscriptionCancelUrl)
                        .build();

        return payOS.paymentRequests().create(paymentData);
    }

    @Override
    public void messageReturnUrl(WebhookData data) {
        String orderCode = String.valueOf(data.getOrderCode());

        Map<String, Object> responseParams = new HashMap<>();
        responseParams.put("orderCode", data.getOrderCode());
        responseParams.put("amount", data.getAmount());
        responseParams.put("description", data.getDescription());
        responseParams.put("accountNumber", data.getAccountNumber());
        responseParams.put("reference", data.getReference());
        responseParams.put("transactionDateTime", data.getTransactionDateTime());
        responseParams.put("currency", data.getCurrency());
        responseParams.put("paymentLinkId", data.getPaymentLinkId());
        responseParams.put("code", data.getCode());
        responseParams.put("desc", data.getDesc());

        responseParams.put("id", data.getPaymentLinkId());

        messagingTemplate.convertAndSend("/topic/payment/" + orderCode, responseParams);
    }
}
