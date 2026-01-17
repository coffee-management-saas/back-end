package com.futurenbetter.saas.modules.subscription.dto.request;

import com.futurenbetter.saas.modules.subscription.enums.BillingCycleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {
    @NotNull(message = "Vui lòng chọn gói dịch vụ")
    private Long subscriptionPlanId;

    @NotNull(message = "Vui lòng chọn chu kỳ thanh toán")
    private BillingCycleEnum billingCycle;

    private String shopName;
    private String address;
    private String phone;
    private String email;
    private String domain;
    private Boolean autoRenewal;
}
