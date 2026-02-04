package com.futurenbetter.saas.modules.employee.repository;

import com.futurenbetter.saas.modules.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByEmployeeIdAndShopId(Long employeeId, Long shopId);

    Page<Employee> findAllByShopId(Long shopId, Pageable pageable);
}
