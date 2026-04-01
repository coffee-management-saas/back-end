package com.futurenbetter.saas.modules.payos.service.impl;

import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Service
@RequiredArgsConstructor
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;

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
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(order.getOrderId())
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
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(subscriptionTransaction.getSubscriptionTransactionId())
                        .description("SUB_PLAN: " + subscriptionTransaction.getShop().getId())
                        .amount(subscriptionTransaction.getAmount())
                        .item(paymentLinkItem)
                        .returnUrl(subscriptionReturnUrl)
                        .cancelUrl(subscriptionCancelUrl)
                        .build();

        return payOS.paymentRequests().create(paymentData);
    }
}
