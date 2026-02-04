package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_check_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    StockCheckSession session;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    RawIngredient ingredient;

    @Column(name = "snapshot_quantity", nullable = false)
    Double snapshotQuantity;

    @Column(name = "actual_quantity")
    Double actualQuantity;

    @Column(name = "diff_quantity")
    Double diffQuantity;

    @Column(name = "reason", columnDefinition = "TEXT")
    String reason;

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
