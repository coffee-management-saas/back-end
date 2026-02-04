package com.futurenbetter.saas.modules.inventory.specification;

import com.futurenbetter.saas.modules.inventory.dto.filter.IngredientBatchFilter;
import com.futurenbetter.saas.modules.inventory.entity.IngredientBatch;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class IngredientBatchSpec {

    public static Specification<IngredientBatch> filter(IngredientBatchFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<IngredientBatch, RawIngredient> ingredientJoin = root.join("rawIngredient", JoinType.INNER);

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (filter.getIngredientId() != null) {
                predicates.add(cb.equal(ingredientJoin.get("id"), filter.getIngredientId()));
            }

            if (StringUtils.hasText(filter.getBatchCode())) {
                predicates
                        .add(cb.like(cb.lower(root.get("batchCode")), "%" + filter.getBatchCode().toLowerCase() + "%"));
            }

            if (filter.getExpiredBeforeDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expiredAt").as(java.time.LocalDate.class),
                        filter.getExpiredBeforeDate()));
            }

            if (Boolean.TRUE.equals(filter.getHasRemainingQuantity())) {
                predicates.add(cb.greaterThan(root.get("currentQuantity"), 0));
            }

            if (filter.getInventoryStatus() != null) {
                predicates.add(cb.equal(root.get("inventoryStatus"), filter.getInventoryStatus()));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}