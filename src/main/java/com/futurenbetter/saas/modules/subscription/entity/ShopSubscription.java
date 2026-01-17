package com.futurenbetter.saas.modules.subscription.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.subscription.enums.BillingCycleEnum;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shop_subscription")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShopSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopSubscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "shopSubscription", cascade = CascadeType.ALL)
    private List<BillingInvoice> billingInvoices;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycleEnum billingCycleStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "price")
    private Long price;

    @Column(name = "auto_renewal")
    private Boolean autoRenewal;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan_status")
    private SubscriptionPlanEnum subscriptionPlanStatus;
}
