package com.futurenbetter.saas.modules.promotion.dto.request;

import com.futurenbetter.saas.modules.promotion.enums.DiscountTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private PromotionEnum status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> productIds;
}
