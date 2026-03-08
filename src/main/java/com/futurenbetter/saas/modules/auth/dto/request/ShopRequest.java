package com.futurenbetter.saas.modules.auth.dto.request;

import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopRequest {
    String shopName;
    String address;
    String phone;
    String email;
    String domain;
    ShopStatus status;
}
