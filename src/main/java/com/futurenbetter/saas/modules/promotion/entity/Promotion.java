package com.futurenbetter.saas.modules.promotion.entity;


import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.promotion.enums.DiscountTypeEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.enums.PromotionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @Column(name = "promotion_code", nullable = false, unique = true)
    private String promotionCode;

    @Column(name = "promotion_name")
    private String promotionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    private PromotionTypeEnum promotionType;

    @Column(name = "minimum_spent")
    private int minimumSpent;

    @Column(name = "quantity")
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountTypeEnum discountType;

    @Column(name = "discount_value")
    private Float discountValue;

    @Column(name = "max_discount_amount")
    private Float maxDiscountAmount;

    @Column(name = "usage_limit_per_user")
    private int usageLimitPerUser;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_status")
    private PromotionEnum promotionStatus;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    private List<PromotionTarget> promotionTargets;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    private List<PromotionUsage> promotionUsages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;
}
