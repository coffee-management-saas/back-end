package com.futurenbetter.saas.modules.order.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.order.enums.OrderStatus;
import com.futurenbetter.saas.modules.order.enums.OrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderFilter extends BaseFilter {
    private OrderStatus status;
    private OrderType orderType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long customerId;
    // private Long createdBy;  // employee
}
