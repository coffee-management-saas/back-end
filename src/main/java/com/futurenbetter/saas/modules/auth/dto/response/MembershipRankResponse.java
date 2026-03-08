package com.futurenbetter.saas.modules.auth.dto.response;

import com.futurenbetter.saas.modules.auth.enums.MembershipRankStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipRankResponse {
    Long id;
    String rankName;
    Float pointRate;
    Integer requiredPoints;
    MembershipRankStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
