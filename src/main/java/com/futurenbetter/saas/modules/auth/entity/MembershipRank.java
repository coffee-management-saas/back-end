package com.futurenbetter.saas.modules.auth.entity;

import com.futurenbetter.saas.modules.auth.enums.MembershipRankStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "membership_rank")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank_name")
    private String rankName;

    @Column(name = "point_rate")
    private Float pointRate;

    @Column(name = "required_point")
    private Integer requiredPoints;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MembershipRankStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "membershipRank", fetch = FetchType.LAZY)
    private List<Customer> customers;
}
