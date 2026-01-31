package com.futurenbetter.saas.modules.employee.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    EmployeeResponse getById(Long employeeId);
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request);
    void deleteEmployee(Long employeeId);
    Page<EmployeeResponse> getAll(BaseFilter filter);
    Employee getEmployeeById(Long employeeId);
}
