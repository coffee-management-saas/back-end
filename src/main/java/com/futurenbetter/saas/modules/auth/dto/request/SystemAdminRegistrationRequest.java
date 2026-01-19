package com.futurenbetter.saas.modules.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SystemAdminRegistrationRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String password;
    private String fullname;

    @Email(message = "Email không hợp lệ")
    private String email;
    private String phone;
    private String address;
    private LocalDateTime dob;
    private LocalDateTime createdAt;

}
