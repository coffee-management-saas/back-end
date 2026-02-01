package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;

public interface PdfExportService {
    byte[] generateInvoicePdf(BillingInvoice billingInvoice);
    byte[] generateOrderPdf(Order order);
}
