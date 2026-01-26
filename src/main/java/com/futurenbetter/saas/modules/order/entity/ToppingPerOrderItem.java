package com.futurenbetter.saas.modules.order.entity;

import com.futurenbetter.saas.modules.order.enums.ToppingPerOrderItemStatus;
import com.futurenbetter.saas.modules.product.entity.Topping;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "topping_per_order_item")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingPerOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topping_per_order_item_id")
    Long toppingPerOrderItemId;

    @Column(name = "quantity")
    int quantity;

    @Column(name = "topping_per_order_item_status")
    @Enumerated(EnumType.STRING)
    ToppingPerOrderItemStatus status;

    @Column(name = "price")
    Long price;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id")
    Topping topping;
}
