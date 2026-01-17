package com.futurenbetter.saas.modules.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomoPaymentResponse {
    private String payUrl;
    private String orderId;
    private String amount;
    private Long invoiceId;
}
