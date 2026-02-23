package com.futurenbetter.saas.modules.auth.dto.filter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopFilter extends BaseFilter {
    String keyword; // name, email, phone, domain
    ShopStatus status;
}
