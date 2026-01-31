package com.futurenbetter.saas.modules.employee.repository;

import com.futurenbetter.saas.modules.employee.entity.ShiftTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate,Long> {
    Optional<ShiftTemplate> findByShiftTemplateIdAndShopId(Long id, Long shopId);
    Page<ShiftTemplate> findAllByShopId(Long shopId, Pageable pageable);
}
