package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.enums.BaseUnit;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "unit_conversions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_unit", nullable = false)
    InputUnit fromUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_unit", nullable = false)
    BaseUnit toUnit;

    @Column(name = "conversion_factor", nullable = false)
    Double conversionFactor;

    @Column(name = "is_standard", nullable = false)
    Boolean isStandard;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    RawIngredient ingredient;

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
        isStandard = false;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
