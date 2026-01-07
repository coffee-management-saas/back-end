package com.futurenbetter.saas.modules.inventory.dto.response;

import com.futurenbetter.saas.modules.inventory.enums.Status;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckSessionResponse {
    Long id;
    String code;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    Status status;
    String createdByName;
    Boolean isApproved;
    List<StockCheckDetailResponse> details;
}
