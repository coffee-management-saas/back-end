package com.futurenbetter.saas.modules.inventory.specification;

import com.futurenbetter.saas.modules.inventory.dto.filter.RawIngredientFilter;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RawIngredientSpec {

    public static Specification<RawIngredient> filter(RawIngredientFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (StringUtils.hasText(filter.getKeyword())) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate nameInfo = cb.like(cb.lower(root.get("name")), keyword);
                Predicate skuInfo = cb.like(cb.lower(root.get("skuCode")), keyword);

                predicates.add(cb.or(nameInfo, skuInfo));
            }

            if (filter.getStorageType() != null) {
                predicates.add(cb.equal(root.get("storageType"), filter.getStorageType()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}