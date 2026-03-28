package com.futurenbetter.saas.modules.payos.service.inter;

import com.futurenbetter.saas.modules.order.entity.Order;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

public interface PayOSService {
    PaymentLinkItem buildPaymentLinkItem(Order order);
    CreatePaymentLinkResponse buildPaymentLink(Order order, PaymentLinkItem paymentLinkItem);
}
