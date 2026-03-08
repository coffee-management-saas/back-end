package com.futurenbetter.saas.modules.inventory.service.inter;

import com.futurenbetter.saas.modules.inventory.dto.request.UnitConversionRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.UnitConversionResponse;
import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;

public interface UnitConversionService {

    UnitConversionResponse create(UnitConversionRequest request);

    UnitConversion update(Long id, UnitConversionRequest request);

    Double convertToBaseUnit(Long ingredientId, InputUnit fromUnit, Double quantity);
}
