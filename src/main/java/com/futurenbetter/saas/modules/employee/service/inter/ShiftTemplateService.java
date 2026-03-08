package com.futurenbetter.saas.modules.employee.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.employee.dto.request.ShiftTemplateRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ShiftTemplateResponse;
import org.springframework.data.domain.Page;

public interface ShiftTemplateService {
    ShiftTemplateResponse create(ShiftTemplateRequest request);

    ShiftTemplateResponse update(Long id, ShiftTemplateRequest request);

    ShiftTemplateResponse getDetail(Long id);

    void delete(Long id);

    Page<ShiftTemplateResponse> getAll(BaseFilter filter);
}