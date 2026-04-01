package com.futurenbetter.saas.modules.payos.service.impl;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;



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
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(order.getOrderId())
                        .description("ORDER: " + order.getOrderId())
                        .amount(order.getPaidPrice())
                        .item(paymentLinkItem)
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
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
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(subscriptionTransaction.getSubscriptionTransactionId())
                        .description("SUB_PLAN: " + subscriptionTransaction.getShop().getId())
                        .amount(subscriptionTransaction.getAmount())
                        .item(paymentLinkItem)
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .build();

        return payOS.paymentRequests().create(paymentData);
    }
}
