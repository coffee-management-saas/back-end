package com.futurenbetter.saas.modules.subscription.entity;

import com.futurenbetter.saas.modules.subscription.enums.PaymentGatewayEnum;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionTransactionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionTransactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_invoice_id", unique = true)
    private BillingInvoice invoice;

    @Column(name = "amount")
    private Long amount;

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
    private SubscriptionTransactionEnum status;
}
