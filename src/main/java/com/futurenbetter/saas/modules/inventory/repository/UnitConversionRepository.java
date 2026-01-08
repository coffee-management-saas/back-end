package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {

    Optional<UnitConversion> findByIdAndId(Long id, Long shopId);

    boolean existsByIngredientIdAndFromUnitAndStatus(Long ingredientId, InputUnit fromUnit, Status status);

    Optional<UnitConversion> findByIngredientIdAndFromUnitAndStatus(Long ingredientId, InputUnit fromUnit, Status status);
}