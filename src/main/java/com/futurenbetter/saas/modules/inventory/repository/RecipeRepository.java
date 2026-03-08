package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByIdAndShopId(Long id, Long shopId);

    List<Recipe> findByVariantIdAndShopId(Long variantId, Long shopId);

    List<Recipe> findByToppingIdAndShopId(Long toppingId, Long shopId);

    List<Recipe> findByVariantIdOrToppingId(Long variantId, Long toppingId);

    @Modifying
    @Query("DELETE FROM Recipe r WHERE r.variantId = :variantId AND r.shop.id = :shopId")
    void deleteByVariantIdAndShopId(Long variantId, Long shopId);

    @Modifying
    @Query("DELETE FROM Recipe r WHERE r.toppingId = :toppingId AND r.shop.id = :shopId")
    void deleteByToppingIdAndShopId(Long toppingId, Long shopId);
}