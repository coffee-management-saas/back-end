package com.futurenbetter.saas.modules.auth.dto.response;

import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {
    Long id;
    String shopName;
    String address;
    String phone;
    String email;
    String domain;
    ShopStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
