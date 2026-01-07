package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.filter.InventoryInvoiceFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.InventoryInvoiceRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.InvoiceItemRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.InventoryInvoiceResponse;
import com.futurenbetter.saas.modules.inventory.entity.*;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.enums.TransactionType;
import com.futurenbetter.saas.modules.inventory.mapper.InventoryInvoiceMapper;
import com.futurenbetter.saas.modules.inventory.mapper.InvoiceItemMapper;
import com.futurenbetter.saas.modules.inventory.repository.*;
import com.futurenbetter.saas.modules.inventory.service.inter.InventoryInvoiceService;
import com.futurenbetter.saas.modules.inventory.service.inter.UnitConversionService;
import com.futurenbetter.saas.modules.inventory.specification.InventoryInvoiceSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryInvoiceServiceImpl implements InventoryInvoiceService {

    private final InventoryInvoiceRepository invoiceRepository;
    private final InventoryInvoiceDetailRepository detailRepository;
    private final IngredientBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final RawIngredientRepository ingredientRepository;

    private final UnitConversionService conversionService;
    private final InventoryInvoiceMapper invoiceMapper;
    private final InvoiceItemMapper itemMapper;

    @Override
    @Transactional
    public InventoryInvoiceResponse importStock(InventoryInvoiceRequest request) {
        var shop = SecurityUtils.getCurrentShop();

        // 1. Tạo Header Invoice dùng Mapper
        InventoryInvoice invoice = invoiceMapper.toEntity(request);
        invoice.setShop(shop);
        invoice.setCreatedBy(SecurityUtils.getCurrentUserId());
        invoice.setStatus(Status.ACTIVE);

        // Lưu tạm để có ID
        invoice = invoiceRepository.save(invoice);

        double totalAmount = 0.0;
        var details = new ArrayList<InventoryInvoiceDetail>();

        // 2. Loop Items
        for (InvoiceItemRequest itemReq : request.getItems()) {
            RawIngredient ingredient = ingredientRepository.findByIdAndShopId(itemReq.getIngredientId(), shop.getId())
                    .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại: " + itemReq.getIngredientId()));

            // Tính toán quy đổi
            Double convertedQty = conversionService.convertToBaseUnit(
                    ingredient.getId(),
                    itemReq.getInputUnit(),
                    Double.valueOf(itemReq.getInputQuantity())
            );

            // Tạo Batch (Logic FIFO)
            IngredientBatch batch = new IngredientBatch();
            batch.setShop(shop);
            batch.setRawIngredient(ingredient);
            batch.setBatchCode(itemReq.getBatchCode());
            batch.setSupplierName(request.getSupplierName());
            batch.setExpiredAt(itemReq.getExpiredAt().atStartOfDay());
            batch.setInitialQuantity(convertedQty);
            batch.setCurrentQuantity(convertedQty);

            // Tính giá vốn: Giá nhập / Số lượng đã quy đổi
            Double costPerBaseUnit = itemReq.getUnitPrice() / (convertedQty / itemReq.getInputQuantity());
            batch.setImportPrice(costPerBaseUnit);
            batch.setStatus(Status.ACTIVE);

            batch = batchRepository.save(batch);

            // Ghi Transaction Log
            InventoryTransaction trans = new InventoryTransaction();
            trans.setShop(shop);
            trans.setIngredient(ingredient);
            trans.setBatch(batch);
            trans.setInvoice(invoice);
            trans.setTransactionType(TransactionType.IMPORT);
            trans.setQuantityChange(convertedQty);
            trans.setQuantityAfter(convertedQty);
            trans.setStatus(Status.ACTIVE);
            transactionRepository.save(trans);

            // Tạo Detail dùng Mapper
            InventoryInvoiceDetail detail = itemMapper.toEntity(itemReq);
            detail.setInventoryInvoice(invoice);
            detail.setRawIngredient(ingredient);
            detail.setConvertedQuantity(convertedQty);
            detail.setSupplierName(request.getSupplierName()); // Denormalize
            detail.setStatus(Status.ACTIVE);

            details.add(detailRepository.save(detail));

            totalAmount += (itemReq.getUnitPrice() * itemReq.getInputQuantity());
        }

        // 3. Update Total Amount
        invoice.setTotalAmount(totalAmount);
        invoice.setDetails(details);
        invoice = invoiceRepository.save(invoice);

        var response = invoiceMapper.toResponse(invoice);

        return response;
    }

    @Override
    public Page<InventoryInvoiceResponse> getAll(InventoryInvoiceFilter filter) {
        return invoiceRepository.findAll(
                InventoryInvoiceSpec.filter(filter, SecurityUtils.getCurrentShopId()),
                filter.getPageable()
        ).map(invoiceMapper::toResponse);
    }

    @Override
    public InventoryInvoiceResponse getDetail(Long id) {
        return invoiceRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .map(invoiceMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Phiếu nhập không tồn tại"));
    }
}
