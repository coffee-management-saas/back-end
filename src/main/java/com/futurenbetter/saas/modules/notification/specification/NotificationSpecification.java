package com.futurenbetter.saas.modules.notification.specification;

import com.futurenbetter.saas.modules.notification.dto.filter.NotificationFilter;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {

    public static Specification<Notification> filter(NotificationFilter filter, Long shopId, Long recipientId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Shop
            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            } else {
                predicates.add(cb.isNull(root.get("shop")));
            }

            // 2. Lọc theo ID người nhận
            if (recipientId != null) {
                predicates.add(cb.equal(root.get("recipientId"), recipientId));
            }

            // 3. Xử lý các điều kiện từ Filter (Bọc filter != null ra ngoài cùng)
            if (filter != null) {
                if (filter.getRecipientType() != null && !filter.getRecipientType().trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("recipientType"), filter.getRecipientType()));
                }

                if (filter.getIsRead() != null) {
                    predicates.add(cb.equal(root.get("isRead"), filter.getIsRead()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
