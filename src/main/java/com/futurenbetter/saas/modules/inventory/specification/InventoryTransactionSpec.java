package com.futurenbetter.saas.modules.inventory.specification;

import com.futurenbetter.saas.modules.inventory.dto.filter.InventoryTransactionFilter;
import com.futurenbetter.saas.modules.inventory.entity.InventoryTransaction;
import com.futurenbetter.saas.modules.inventory.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionSpec {

    public static Specification<InventoryTransaction> filter(InventoryTransactionFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (filter.getIngredientId() != null) {
                predicates.add(cb.equal(root.get("ingredient").get("id"), filter.getIngredientId()));
            }

            if (filter.getBatchId() != null) {
                predicates.add(cb.equal(root.get("batch").get("id"), filter.getBatchId()));
            }

            if (StringUtils.hasText(filter.getTransactionType())) {
                predicates.add(cb.equal(root.get("transactionType"), TransactionType.valueOf(filter.getTransactionType())));
            }

            if (StringUtils.hasText(filter.getReferenceCode())) {
                Predicate orderIdPred = cb.like(root.get("orderId").as(String.class), "%" + filter.getReferenceCode() + "%");
                predicates.add(orderIdPred);
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}