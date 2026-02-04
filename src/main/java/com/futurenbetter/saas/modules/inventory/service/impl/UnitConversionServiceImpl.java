package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.UnitConversionResponse;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.mapper.UnitConversionMapper;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.repository.UnitConversionRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.UnitConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnitConversionServiceImpl implements UnitConversionService {

    private final UnitConversionRepository repository;
    private final RawIngredientRepository ingredientRepository;
    private final UnitConversionMapper mapper;

    @Override
    @Transactional
    public UnitConversionResponse create(UnitConversionRequest request) {
        Long shopId = SecurityUtils.getCurrentShopId();
        var ingredient = ingredientRepository
                .findByIdAndShopId(request.getIngredientId(), shopId)
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        if (repository.existsByIngredientIdAndFromUnitAndInventoryStatus(ingredient.getId(), request.getFromUnit(),
                InventoryStatus.ACTIVE)) {
            throw new BusinessException("Đơn vị " + request.getFromUnit() + " đã được cấu hình cho nguyên liệu này");
        }

        UnitConversion entity = mapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setIngredient(ingredient);
        entity.setInventoryStatus(InventoryStatus.ACTIVE);

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UnitConversion update(Long id, UnitConversionRequest request) {
        UnitConversion entity = repository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Cấu hình quy đổi không tồn tại"));

        mapper.updateFromRequest(entity, request);

        return repository.save(entity);
    }

    @Override
    public Double convertToBaseUnit(Long ingredientId, InputUnit fromUnit, Double quantity) {
        return repository
                .findByIngredientIdAndFromUnitAndInventoryStatus(ingredientId, fromUnit, InventoryStatus.ACTIVE)
                .map(conversion -> quantity * conversion.getConversionFactor())
                .orElseThrow(() -> new BusinessException("Chưa cấu hình quy đổi cho đơn vị: " + fromUnit));
    }
}