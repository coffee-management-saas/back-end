package com.futurenbetter.saas.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSnapshot {
    private String shopName;
    private String address;
    private String phone;
    private String email;
    private String domain;
}
