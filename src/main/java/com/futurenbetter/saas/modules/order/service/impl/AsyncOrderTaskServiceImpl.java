package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.repository.OrderRepository;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOrderTaskServiceImpl {

    private final PdfExportService pdfExportService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final OrderRepository orderRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateAndUploadInvoice(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return;
            }

            byte[] pdfData = pdfExportService.generateOrderPdf(order);
            String fileName = "INV_" + order.getOrderId() + "_" + System.currentTimeMillis();
            String invoiceUrl = cloudinaryStorageService.uploadFile(pdfData, fileName, "order_invoices");

            order.setInvoiceUrl(invoiceUrl);
            orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
