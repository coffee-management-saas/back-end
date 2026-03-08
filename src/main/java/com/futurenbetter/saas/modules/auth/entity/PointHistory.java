package com.futurenbetter.saas.modules.auth.entity;

import com.futurenbetter.saas.modules.auth.enums.PointHistoryEnum;
import com.futurenbetter.saas.modules.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Column(name = "before_points")
    private Integer beforePoints;

    @Column(name = "after_points")
    private Integer afterPoints;

    @Column(name = "point_change")
    private Integer pointChange;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "point_history_status")
    @Enumerated(EnumType.STRING)
    private PointHistoryEnum pointHistoryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
