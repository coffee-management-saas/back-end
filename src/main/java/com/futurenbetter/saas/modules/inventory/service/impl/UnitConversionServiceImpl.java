package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.Status;
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
    public UnitConversion create(UnitConversionRequest request) {
        var ingredient = ingredientRepository.findByIdAndId(request.getIngredientId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        if (repository.existsByIngredientIdAndFromUnitAndStatus(ingredient.getId(), request.getFromUnit(), Status.ACTIVE)) {
            throw new BusinessException("Đơn vị " + request.getFromUnit() + " đã được cấu hình cho nguyên liệu này");
        }

        UnitConversion entity = mapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setIngredient(ingredient);
        entity.setStatus(Status.ACTIVE);

        return repository.save(entity);
    }


    @Override
    @Transactional
    public UnitConversion update(Long id, UnitConversionRequest request) {
        UnitConversion entity = repository.findByIdAndId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Cấu hình quy đổi không tồn tại"));

        mapper.updateFromRequest(entity, request);

        return repository.save(entity);
    }


    @Override
    public Double convertToBaseUnit(Long ingredientId, InputUnit fromUnit, Double quantity) {
        return repository.findByIngredientIdAndFromUnitAndStatus(ingredientId, fromUnit, Status.ACTIVE)
                .map(conversion -> quantity * conversion.getConversionFactor())
                .orElseThrow(() -> new BusinessException("Chưa cấu hình quy đổi cho đơn vị: " + fromUnit));
    }
}