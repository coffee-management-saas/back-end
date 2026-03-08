package com.futurenbetter.saas.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class SystemAdminRegistrationResponse {
    private Long userProfileId;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
    private LocalDateTime createdAt;
}
