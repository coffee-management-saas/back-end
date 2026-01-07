package com.futurenbetter.saas.modules.inventory.mapper;

import com.futurenbetter.saas.modules.inventory.dto.request.InvoiceItemRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InvoiceItemResponse;
import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoiceDetail;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InvoiceItemMapper {

    @Mapping(source = "rawIngredient.id", target = "ingredientId")
    @Mapping(source = "rawIngredient.name", target = "ingredientName")
    @Mapping(source = "rawIngredient.baseUnit", target = "baseUnit")
    InvoiceItemResponse toResponse(InventoryInvoiceDetail entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventoryInvoice", ignore = true)
    @Mapping(target = "rawIngredient", ignore = true)
    @Mapping(target = "convertedQuantity", ignore = true)
    @Mapping(target = "supplierName", ignore = true)
    InventoryInvoiceDetail toEntity(InvoiceItemRequest request);
}