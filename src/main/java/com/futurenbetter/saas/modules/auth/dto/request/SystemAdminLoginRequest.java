package com.futurenbetter.saas.modules.auth.dto.request;

import lombok.Data;

@Data
public class SystemAdminLoginRequest {
    private String username;
    private String password;
}
