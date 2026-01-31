package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByIdAndShopId(Long id, Long shopId);

    List<Recipe> findByVariantIdAndShopId(Long variantId, Long shopId);

    List<Recipe> findByToppingIdAndShopId(Long toppingId, Long shopId);

    List<Recipe> findByVariantIdOrToppingId(Long variantId, Long toppingId);
}