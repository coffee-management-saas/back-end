package com.futurenbetter.saas.modules.subcription.entity;

import com.futurenbetter.saas.modules.subcription.enums.SubcriptionPlanEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subcription_plan")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubcriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subcriptionPlanId;

    @Column(name = "subcription_plan_name")
    private String subcriptionPlanName;

    @Column(name = "subcription_plan_description")
    private String subcriptionPlanDescription;

    @Column(name = "price_monthly")
    private Float priceMonthly;

    @Column(name = "price_yearly")
    private Float priceYearly;

    @Column(name = "config_limit", columnDefinition = "jsonb")
    private String configLimit;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubcriptionPlanEnum subcriptionPlanStatus;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ShopSubcription> shopSubcriptions;
}
