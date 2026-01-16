package com.futurenbetter.saas.modules.subcription.entity;

import com.futurenbetter.saas.modules.subcription.enums.PaymentGatewayEnum;
import com.futurenbetter.saas.modules.subcription.enums.SubcriptionTransactionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subcription_transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubcriptionTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subcriptionTransactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_invoice_id", unique = true)
    private BillingInvoice invoice;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "is_income")
    private Boolean isIncome;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway")
    private PaymentGatewayEnum paymentGateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubcriptionTransactionEnum status;
}
