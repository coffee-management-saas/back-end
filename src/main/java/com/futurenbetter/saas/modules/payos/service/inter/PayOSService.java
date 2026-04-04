package com.futurenbetter.saas.modules.payos.service.inter;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.util.Map;

public interface PayOSService {
    PaymentLinkItem buildPaymentLinkOrderItem(Order order);
    CreatePaymentLinkResponse buildPaymentLinkOrder(Order order, PaymentLinkItem paymentLinkItem);
    PaymentLinkItem buildPaymentLinkSubscriptionItem(SubscriptionTransaction subscriptionTransaction);
    CreatePaymentLinkResponse buildPaymentLinkSubscription(SubscriptionTransaction subscriptionTransaction, PaymentLinkItem paymentLinkItem);
    void messageReturnUrl(WebhookData data);
}
