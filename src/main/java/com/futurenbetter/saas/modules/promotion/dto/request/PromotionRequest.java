package com.futurenbetter.saas.modules.promotion.dto.request;

import com.futurenbetter.saas.modules.promotion.enums.DiscountTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    private String promotionName;
    private String promotionCode;
    private PromotionTypeEnum promotionType;
    private int minimumSpent;
    private int quantity;
    private DiscountTypeEnum discountType;
    private Float discountValue;
    private Float maxDiscountAmount;
    private int usageLimitPerUser;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long shopId;
//    private List<Long> productIds;
}
