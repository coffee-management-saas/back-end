package com.futurenbetter.saas.modules.promotion.dto.response;

import com.futurenbetter.saas.modules.promotion.enums.DiscountTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    private Long promotionId;
    private String promotionCode;
    private String promotionName;
    private PromotionTypeEnum promotionType;
    private int minimumSpent;
    private int quantity;
    private String imageUrl;
    private DiscountTypeEnum discountType;
    private Float discountValue;
    private Float maxDiscountAmount;
    private int usageLimitPerUser;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PromotionEnum promotionStatus;
    private Long shopId;
//    private List<Long> appliedProductIds;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
