package com.futurenbetter.saas.modules.auth.spec;

import com.futurenbetter.saas.modules.auth.dto.filter.CustomerFilter;
import com.futurenbetter.saas.modules.auth.entity.Customer;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {
    public static Specification<Customer> filter(CustomerFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn luôn filter theo shopId để đảm bảo Multi-tenancy
            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (filter.getRankId() != null) {
                predicates.add(cb.equal(root.get("membershipRank").get("id"), filter.getRankId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
