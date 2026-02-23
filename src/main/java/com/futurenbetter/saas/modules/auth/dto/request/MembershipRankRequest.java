package com.futurenbetter.saas.modules.auth.dto.request;

import com.futurenbetter.saas.modules.auth.enums.MembershipRankStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipRankRequest {
    String rankName;
    Float pointRate;
    Integer requiredPoints;
    MembershipRankStatus status;
}
