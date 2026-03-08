package com.futurenbetter.saas.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long shopId;
    private String role;
}
