package com.futurenbetter.saas.modules.subcription.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.subcription.enums.InvoiceEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "billing_invoice")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BillingInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billingInvoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcription_id")
    private ShopSubcription shopSubcription;

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private SubcriptionTransaction transaction;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status")
    private InvoiceEnum status;
}
