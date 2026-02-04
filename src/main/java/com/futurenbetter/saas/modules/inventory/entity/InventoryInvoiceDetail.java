package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_invoice_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryInvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    InventoryInvoice inventoryInvoice;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    RawIngredient rawIngredient;

    @Column(name = "supplier_name")
    String supplierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_unit", nullable = false)
    InputUnit inputUnit;

    @Column(name = "input_quantity", nullable = false)
    Double inputQuantity;

    @Column(name = "converted_quantity", nullable = false)
    Double convertedQuantity;

    @Column(name = "unit_price", nullable = false)
    Double unitPrice;

    @Column(name = "batch_code", length = 50)
    String batchCode;

    @Column(name = "expired_at", nullable = false)
    LocalDateTime expiredAt;

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
