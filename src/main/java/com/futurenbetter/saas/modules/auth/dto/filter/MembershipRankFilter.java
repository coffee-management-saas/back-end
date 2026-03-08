package com.futurenbetter.saas.modules.auth.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.auth.enums.MembershipRankStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipRankFilter extends BaseFilter {
    String keyword; // rank
    MembershipRankStatus status;
}
