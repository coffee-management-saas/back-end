package com.futurenbetter.saas.modules.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private Long customerId;
    private String username;
    private String fullname;
    private String rankId;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
