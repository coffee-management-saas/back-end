package com.futurenbetter.saas.modules.subscription.entity;

import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subscription_plan")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionPlanId;

    @Column(name = "subscription_plan_name")
    private String subscriptionPlanName;

    @Column(name = "subscription_plan_description")
    private String subscriptionPlanDescription;

    @Column(name = "price_monthly")
    private Long priceMonthly;

    @Column(name = "price_yearly")
    private Long priceYearly;

    @Column(name = "config_limit", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configLimit;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubscriptionPlanEnum subscriptionPlanStatus;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ShopSubscription> shopSubscriptions;
}
