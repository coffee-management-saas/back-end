package com.futurenbetter.saas.modules.employee.repository;

import com.futurenbetter.saas.modules.employee.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
    Optional<Schedule> findByScheduleIdAndShopId(Long id, Long shopId);
    Page<Schedule> findAllByShopId(Long shopId, Pageable pageable);
    List<Schedule> findAllByEmployee_EmployeeIdAndShopId(Long employeeId, Long shopId);
}
