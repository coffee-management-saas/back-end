package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByIdAndShopId(Long id, Long shopId);
    boolean existsByNameAndShopId(String name, Long shopId);
}