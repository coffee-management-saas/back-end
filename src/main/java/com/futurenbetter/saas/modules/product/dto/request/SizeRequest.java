package com.futurenbetter.saas.modules.product.dto.request;

import com.futurenbetter.saas.modules.product.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeRequest {
    @NotBlank(message = "Mã kích thước không được để trống")
    String code; // S, M, L

    Status status;
}