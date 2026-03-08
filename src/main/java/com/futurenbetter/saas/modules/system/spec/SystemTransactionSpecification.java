package com.futurenbetter.saas.modules.system.spec;

import com.futurenbetter.saas.modules.system.dto.filter.SystemTransactionFilter;
import com.futurenbetter.saas.modules.system.entity.SystemTransaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.util.ArrayList;
import java.util.List;

public class SystemTransactionSpecification {
    public static Specification<SystemTransaction> filter(SystemTransactionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getApproverId() != null) {
                    predicates.add(cb.equal(root.get("approver").get("userProfileId"), filter.getApproverId()));
                }

                if (filter.getIsIncome() != null) {
                    predicates.add(cb.equal(root.get("isIncome"), filter.getIsIncome()));
                }

                if (filter.getType() != null) {
                    predicates.add(cb.equal(root.get("type"), filter.getType()));
                }

                if (filter.getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
                }

                if (filter.getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
