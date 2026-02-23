package com.futurenbetter.saas.modules.auth.spec;

import com.futurenbetter.saas.modules.auth.dto.filter.MembershipRankFilter;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MembershipRankSpecification {
    public static Specification<MembershipRank> filter(Long shopId, MembershipRankFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn luôn filter theo shopId để đảm bảo Multi-tenancy
            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if (StringUtils.hasText(filter.getKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("rankName")), "%" + filter.getKeyword().toLowerCase() + "%"));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
