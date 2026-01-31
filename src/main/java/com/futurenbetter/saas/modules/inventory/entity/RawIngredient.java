package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.enums.StorageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_ingredients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RawIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "sku_code", nullable = false, length = 50)
    String skuCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_unit", nullable = false)
    BaseUnit baseUnit;

    @Column(name = "min_stock_alert")
    Double minStockAlert;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    StorageType storageType;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    InventoryStatus inventoryStatus;

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
