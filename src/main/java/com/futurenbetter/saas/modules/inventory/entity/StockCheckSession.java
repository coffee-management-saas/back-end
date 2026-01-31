package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_check_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(name = "code", length = 50)
    String code;

    @Column(name = "created_by", nullable = false)
    Long createdBy;

    @Column(name = "approved_by")
    Long approvedBy;

    @Column(name = "is_approved", nullable = false)
    Boolean isApproved;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "completed_at", nullable = false)
    LocalDateTime completedAt;

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
        if(isApproved == null) {
            isApproved = false;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
