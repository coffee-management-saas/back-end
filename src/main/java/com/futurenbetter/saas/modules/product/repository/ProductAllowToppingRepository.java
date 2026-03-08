package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.ProductAllowTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAllowToppingRepository extends JpaRepository<ProductAllowTopping, Long> {
    List<ProductAllowTopping> findByProductId(Long productId);
    Optional<ProductAllowTopping> findByProductIdAndToppingId(Long productId, Long toppingId);
    void deleteByProductId(Long productId);
}