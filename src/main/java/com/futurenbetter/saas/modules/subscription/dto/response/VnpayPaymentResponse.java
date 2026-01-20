package com.futurenbetter.saas.modules.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VnpayPaymentResponse {
    private String paymentUrl;
    private String orderId;
    private String amount;
}
