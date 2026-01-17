package com.futurenbetter.saas.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemAdminLoginResponse {
    private String accessToken;
    private String refreshToken;
}
