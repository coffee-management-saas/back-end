package com.futurenbetter.saas.modules.auth.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerFilter extends BaseFilter {
    Long rankId;
}
