package com.futurenbetter.saas.modules.inventory.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inventory_invoices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(name = "created_by", nullable = false)
    Long createdBy;

    @Column(name = "code", nullable = false, length = 50)
    String code;

    @Column(name = "total_amount", nullable = false)
    Double totalAmount;

    @Column(name = "invoice_image_url", columnDefinition = "TEXT")
    String invoiceImageUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;

    @OneToMany(mappedBy = "inventoryInvoice", cascade = CascadeType.ALL)
    private List<InventoryInvoiceDetail> details;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
