package com.futurenbetter.saas.modules.inventory.service.inter;

import com.futurenbetter.saas.modules.inventory.dto.filter.InventoryInvoiceFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.InventoryInvoiceRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InventoryInvoiceResponse;
import org.springframework.data.domain.Page;

public interface InventoryInvoiceService {
    InventoryInvoiceResponse importStock(InventoryInvoiceRequest request);
    Page<InventoryInvoiceResponse> getAll(InventoryInvoiceFilter filter);
    InventoryInvoiceResponse getDetail(Long id);
}