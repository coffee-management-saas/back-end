package com.futurenbetter.saas.modules.auth.entity;

import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.entity.PromotionTarget;
import com.futurenbetter.saas.modules.promotion.entity.PromotionUsage;
import com.futurenbetter.saas.modules.subcription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subcription.entity.ShopSubcription;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "domain")
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShopStatus shopStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<MembershipRank> membershipRanks;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<PromotionUsage> promotionUsages;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<PromotionTarget> promotionTargets;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Promotion> promotions;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<ShopSubcription> shopSubcriptions;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<BillingInvoice> billingInvoices;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserProfile owner;
}
