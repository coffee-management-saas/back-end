package com.futurenbetter.saas.modules.order.entity;

import com.futurenbetter.saas.modules.order.enums.OrderItemStatus;
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "order_item")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    Long orderItemId;

    @Column(name = "unit_price")
    Long unitPrice;

    @Column(name = "order_item_status")
    @Enumerated(EnumType.STRING)
    OrderItemStatus orderItemStatus;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    ProductVariant productVariant;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    List<ToppingPerOrderItem> toppingPerOrderItems;
}
