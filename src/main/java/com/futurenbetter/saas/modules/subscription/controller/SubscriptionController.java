package com.futurenbetter.saas.modules.subscription.controller;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.VnpayPaymentResponse;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
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

    @PostMapping("/vnpay")
    public ResponseEntity<VnpayPaymentResponse> createVnpayPayment(
            @Valid @RequestBody SubscriptionRequest request,
            HttpServletRequest httpServletRequest
    ) throws UnsupportedEncodingException {
        String ipAddress = httpServletRequest.getRemoteAddr();
        return ResponseEntity.ok(subscriptionService.createSubscriptionWithVnpay(request,ipAddress));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            subscriptionService.handleVnpayReturn(params);

            return ResponseEntity.ok("Payment successful");
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}
