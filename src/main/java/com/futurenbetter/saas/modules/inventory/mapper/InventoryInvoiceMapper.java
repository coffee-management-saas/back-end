package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.InventoryInvoiceRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InventoryInvoiceResponse;
import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoice;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {InvoiceItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InventoryInvoiceMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdByName", ignore = true)
    InventoryInvoiceResponse toResponse(InventoryInvoice entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    InventoryInvoice toEntity(InventoryInvoiceRequest request);
}