package com.futurenbetter.saas.modules.notification.specification;

import com.futurenbetter.saas.modules.notification.dto.filter.NotificationFilter;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {
    public static Specification<Notification> filter(NotificationFilter filter, Long shopId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("shop").get("id"), shopId));

            if(filter.getIsRead() != null) {
                predicates.add(cb.equal(root.get("isRead"), filter.getIsRead()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
