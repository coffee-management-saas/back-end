package com.futurenbetter.saas.modules.inventory.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryInvoiceResponse {
    Long id;
    String code;
    String supplierName;
    Double totalAmount;
    String invoiceImageUrl;
    LocalDateTime importedAt;
    String createdByName;
    List<InvoiceItemResponse> items;
}
