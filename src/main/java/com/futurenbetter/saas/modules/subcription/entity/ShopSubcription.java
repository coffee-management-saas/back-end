package com.futurenbetter.saas.modules.subcription.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.subcription.enums.BillingCycleEnum;
import com.futurenbetter.saas.modules.subcription.enums.SubcriptionPlanEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shop_subcription")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShopSubcription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopSubcriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcription_plan_id")
    private SubcriptionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "shopSubcription", cascade = CascadeType.ALL)
    private List<BillingInvoice> billingInvoices;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycleEnum billingCycleStautus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "price")
    private Float price;

    @Column(name = "auto_renewal")
    private Boolean autoRenewal;

    @Enumerated(EnumType.STRING)
    @Column(name = "subcription_plan_status")
    private SubcriptionPlanEnum subcriptionPlanStatus;
}
