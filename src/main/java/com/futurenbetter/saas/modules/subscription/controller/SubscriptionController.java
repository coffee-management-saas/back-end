package com.futurenbetter.saas.modules.subscription.controller;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final BillingInvoiceRepository billingInvoiceRepository;

    @PostMapping("/checkout")
    public ResponseEntity<MomoPaymentResponse> checkout(
            @Valid @RequestBody SubscriptionRequest request
            ) {
        MomoPaymentResponse response = subscriptionService.createSubscriptionWithMomo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/momo-callback")
    public ResponseEntity<?> momoCallback(
            @RequestParam Map<String, String> payload
            ) {
        subscriptionService.handleMomoIpn(payload);
        return ResponseEntity.ok("Thành công");
    }

    @GetMapping("/invoice/{id}/download")
    public ResponseEntity<String> downloadInvoice(@PathVariable Long id) {
        BillingInvoice invoice = billingInvoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Hóa đơn không tồn tại"));

        if(invoice.getPdfUrl() == null) {
            throw new BusinessException("Hóa đơn này chưa được xuất pdf");
        }

        return ResponseEntity.ok(invoice.getPdfUrl());
    }
}
