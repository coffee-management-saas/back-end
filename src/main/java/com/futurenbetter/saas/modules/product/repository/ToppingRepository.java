package com.futurenbetter.saas.modules.product.repository;

import com.futurenbetter.saas.modules.product.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long>, JpaSpecificationExecutor<Topping> {
    Optional<Topping> findByIdAndShopId(Long id, Long shopId);
}