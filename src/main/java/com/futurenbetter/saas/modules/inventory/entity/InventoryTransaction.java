package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions", indexes = {
        @Index(name = "idx_inv_trans_shop_ingredient_date",
                columnList = "shop_id, ingredient_id, created_at"),
        @Index(name = "idx_inv_trans_shop_batch", columnList = "shop_id, batch_id"),
        @Index(name = "idx_inv_trans_shop_order", columnList = "shop_id, order_id"),
        @Index(name = "idx_inv_trans_shop_type_date", columnList = "shop_id, transaction_type, created_at"),
        @Index(name = "idx_inv_trans_created_at", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    RawIngredient ingredient;

    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    IngredientBatch batch;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    InventoryInvoice invoice;

    @ManyToOne
    @JoinColumn(name = "stock_check_detail_id")
    StockCheckDetail stockCheckDetail;

    // @ManyToOne
    @Column(name = "order_id")
    Long orderId;

    @Column(name = "quantity_change", nullable = false)
    Double quantityChange;

    @Column(name = "quantity_after", nullable = false)
    Double quantityAfter;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    InventoryStatus inventoryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    TransactionType transactionType;

    @PrePersist
    public void onCreate() {
        if (inventoryStatus == null) {
            inventoryStatus = InventoryStatus.ACTIVE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
