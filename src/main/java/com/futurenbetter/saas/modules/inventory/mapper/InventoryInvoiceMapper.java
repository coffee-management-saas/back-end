package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.InventoryInvoiceRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InventoryInvoiceResponse;
import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        InvoiceItemMapper.class })
public interface InventoryInvoiceMapper {

    @Mapping(target = "items", source = "details")
    @Mapping(target = "importedAt", source = "createdAt")
    @Mapping(target = "supplierName", expression = "java(entity.getDetails() != null && !entity.getDetails().isEmpty() ? entity.getDetails().get(0).getSupplierName() : null)")
    @Mapping(target = "createdByName", ignore = true)
    InventoryInvoiceResponse toResponse(InventoryInvoice entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)
    InventoryInvoice toEntity(InventoryInvoiceRequest request);
}