package com.futurenbetter.saas.modules.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopAdminRegistrationRequest extends SystemAdminRegistrationRequest{
    private Long shopId;
}
