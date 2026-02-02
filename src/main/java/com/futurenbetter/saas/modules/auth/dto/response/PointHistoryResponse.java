package com.futurenbetter.saas.modules.auth.dto.response;

import com.futurenbetter.saas.modules.auth.enums.PointHistoryEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointHistoryResponse {
    private Long id;
    private Integer beforePoints;
    private Integer changePoints;
    private Integer afterPoints;
    private PointHistoryEnum pointHistoryStatus;
    private Long orderId;
    private LocalDateTime createdAt;
}
