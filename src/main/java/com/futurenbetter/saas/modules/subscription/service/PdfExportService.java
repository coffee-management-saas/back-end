package com.futurenbetter.saas.modules.subscription.service;

import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;

public interface PdfExportService {

    byte[] generateInvoicePdf(BillingInvoice billingInvoice);
}
