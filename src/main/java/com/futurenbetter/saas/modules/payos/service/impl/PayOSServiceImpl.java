package com.futurenbetter.saas.modules.payos.service.impl;

import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    public PaymentLinkItem buildPaymentLinkItem(Order order) {
        return PaymentLinkItem.builder()
                .name("Thanh toan don hang " + order.getOrderId())
                .quantity(1)
                .price(order.getPaidPrice())
                .build();
    }

    @Override
    public CreatePaymentLinkResponse buildPaymentLink(Order order, PaymentLinkItem paymentLinkItem) {
        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(order.getOrderId())
                        .description("Thanh toan don hang " + order.getOrderId())
                        .amount(order.getPaidPrice())
                        .item(paymentLinkItem)
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .build();

        return payOS.paymentRequests().create(paymentData);
    }

}
