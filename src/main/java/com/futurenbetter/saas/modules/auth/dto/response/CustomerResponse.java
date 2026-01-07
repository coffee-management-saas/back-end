package com.futurenbetter.saas.modules.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private String customerId;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String dob;
    private LocalDateTime createdAt;
    private String status;
}
