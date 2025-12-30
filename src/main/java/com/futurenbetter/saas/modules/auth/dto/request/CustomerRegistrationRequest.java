package com.futurenbetter.saas.modules.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationRequest {
    private String username;
    private String fullname;
    private String phone;
    private String email;
    private String address;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dob;
}
