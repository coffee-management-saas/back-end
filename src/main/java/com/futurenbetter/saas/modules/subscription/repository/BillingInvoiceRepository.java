package com.futurenbetter.saas.modules.subscription.repository;

import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, Long> {
}
