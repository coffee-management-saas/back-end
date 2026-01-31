package com.futurenbetter.saas.modules.employee.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeUnavailabilityRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeUnavailabilityResponse;
import org.springframework.data.domain.Page;

public interface EmployeeUnavailabilityService {

    EmployeeUnavailabilityResponse create(EmployeeUnavailabilityRequest request);

    EmployeeUnavailabilityResponse update(Long id, EmployeeUnavailabilityRequest request);

    EmployeeUnavailabilityResponse getDetail(Long id);

    void delete(Long id);

    Page<EmployeeUnavailabilityResponse> getAll(BaseFilter filter);
}