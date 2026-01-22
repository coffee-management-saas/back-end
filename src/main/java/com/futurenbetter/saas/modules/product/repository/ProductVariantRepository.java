package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
    Optional<ProductVariant> findByIdAndShopId(Long id, Long shopId);

    Optional<ProductVariant> findBySkuCodeAndShopId(String skuCode, Long shopId);

    List<ProductVariant> findAllByProductIdAndShopId(Long productId, Long shopId);

    boolean existsBySkuCodeAndShopId(String skuCode, Long shopId);
}