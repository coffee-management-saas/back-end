package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    Optional<Size> findByIdAndShopId(Long id, Long shopId);
    boolean existsByCodeAndShopId(String code, Long shopId);
}