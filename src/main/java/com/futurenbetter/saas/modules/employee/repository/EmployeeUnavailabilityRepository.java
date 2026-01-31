package com.futurenbetter.saas.modules.employee.repository;

import com.futurenbetter.saas.modules.employee.entity.EmployeeUnavailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeUnavailabilityRepository extends JpaRepository<EmployeeUnavailability,Long> {
    Optional<EmployeeUnavailability> findByEmployeeUnavailabilityIdAndShopId(Long id, Long shopId);
    Page<EmployeeUnavailability> findAllByShopId(Long shopId, Pageable pageable);
}
