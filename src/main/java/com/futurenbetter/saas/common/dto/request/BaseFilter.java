package com.futurenbetter.saas.common.dto.request;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseFilter {
    private int page = 0;
    private int size = 10;
    private List<String> sort;

    public Pageable getPageable() {
        return PageRequest.of(page, size, getSort());
    }

    private Sort getSort() {
        if (CollectionUtils.isEmpty(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (String sortParam : sort) {
            String[] parts = sortParam.split(":");
            String property = parts[0];

            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }

            orders.add(new Sort.Order(direction, property));
        }

        return Sort.by(orders);
    }
}
