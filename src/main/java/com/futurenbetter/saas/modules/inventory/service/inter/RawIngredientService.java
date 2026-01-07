package com.futurenbetter.saas.modules.inventory.service.inter;

import com.futurenbetter.saas.modules.inventory.dto.filter.RawIngredientFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.RawIngredientRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RawIngredientResponse;
import org.springframework.data.domain.Page;

public interface RawIngredientService {
    RawIngredientResponse create(RawIngredientRequest request);
    RawIngredientResponse update(Long id, RawIngredientRequest request);
    Page<RawIngredientResponse> getAll(RawIngredientFilter filter);
    RawIngredientResponse getDetail(Long id);
}
