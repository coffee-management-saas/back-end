package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
    boolean existsByNameAndShopId(String name, Long shopId);
}