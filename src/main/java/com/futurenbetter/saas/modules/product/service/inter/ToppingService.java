package com.futurenbetter.saas.modules.product.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.product.dto.request.ToppingRequest;
import com.futurenbetter.saas.modules.product.dto.response.ToppingResponse;
import org.springframework.data.domain.Page;

public interface ToppingService {
    ToppingResponse create(ToppingRequest request);
    ToppingResponse update(Long id, ToppingRequest request);
    ToppingResponse getDetail(Long id);
    Page<ToppingResponse> getAll(BaseFilter filter);
    void delete(Long id);
}