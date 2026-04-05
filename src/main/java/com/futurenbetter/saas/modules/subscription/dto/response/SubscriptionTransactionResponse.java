package com.futurenbetter.saas.modules.subscription.dto.response;

import com.futurenbetter.saas.modules.order.enums.PaymentGateway;
import com.futurenbetter.saas.modules.subscription.enums.BillingCycleEnum;
import com.futurenbetter.saas.modules.subscription.enums.PaymentGatewayEnum;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionTransactionEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionTransactionResponse {
    Long subscriptionTransactionId;
    Long subscriptionPlanId;
    Long billingInvoiceId;
    Long shopId;
    Long amount;
    BillingCycleEnum billingCycle;
    PaymentGatewayEnum paymentGateway;
    SubscriptionTransactionEnum status;
}
