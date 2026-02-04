package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.UnitConversion;
import com.futurenbetter.saas.modules.inventory.enums.InputUnit;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {

        Optional<UnitConversion> findByIdAndShopId(Long id, Long shopId);

        boolean existsByIngredientIdAndFromUnitAndInventoryStatus(Long ingredientId, InputUnit fromUnit,
                        InventoryStatus inventoryStatus);

        Optional<UnitConversion> findByIngredientIdAndFromUnitAndInventoryStatus(Long ingredientId, InputUnit fromUnit,
                        InventoryStatus inventoryStatus);
}