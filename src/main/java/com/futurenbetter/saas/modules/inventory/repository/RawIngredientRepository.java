package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RawIngredientRepository extends JpaRepository<RawIngredient, Long>, JpaSpecificationExecutor<RawIngredient> {

    Optional<RawIngredient> findByIdAndId(Long id, Long shopId);

    List<RawIngredient> findAllById(Long shopId);
}
