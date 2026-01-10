package com.futurenbetter.saas.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SystemAdminRegistrationResponse {
    private Long userProfileId;
    private String username;
    private String fullname;
    private String email;
    private String phone;
}
