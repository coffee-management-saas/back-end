package com.futurenbetter.saas.modules.employee.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.employee.dto.request.ScheduleRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ScheduleResponse;
import org.springframework.data.domain.Page;


import java.util.List;

public interface ScheduleService {
    ScheduleResponse create(ScheduleRequest request);

    ScheduleResponse update(Long id, ScheduleRequest request);

    ScheduleResponse getDetail(Long id);

    void delete(Long id);

    Page<ScheduleResponse> getAll(BaseFilter filter);

    List<ScheduleResponse> getByEmployeeId(Long employeeId);
}