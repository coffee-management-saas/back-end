package com.futurenbetter.saas.modules.inventory.specification;

import com.futurenbetter.saas.modules.inventory.dto.filter.StockCheckSessionFilter;
import com.futurenbetter.saas.modules.inventory.entity.StockCheckSession;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StockCheckSessionSpec {

    public static Specification<StockCheckSession> filter(StockCheckSessionFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (StringUtils.hasText(filter.getCode())) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            if (filter.getCreatedBy() != null) {
                predicates.add(cb.equal(root.get("createdBy"), filter.getCreatedBy()));
            }

            if (filter.getIsApproved() != null) {
                predicates.add(cb.equal(root.get("isApproved"), filter.getIsApproved()));
            }

            if (filter.getInventoryStatus() != null) {
                predicates.add(cb.equal(root.get("inventoryStatus"), filter.getInventoryStatus()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}