package com.futurenbetter.saas.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryInvoiceRequest {
    @NotBlank(message = "Mã phiếu không được để trống")
    String code;
    String supplierName;
    String invoiceImageUrl;
    String note;
    @NotEmpty(message = "Danh sách hàng nhập không được trống")
    List<InvoiceItemRequest> items;
}
