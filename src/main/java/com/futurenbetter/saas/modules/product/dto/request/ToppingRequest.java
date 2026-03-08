package com.futurenbetter.saas.modules.product.dto.request;

import com.futurenbetter.saas.modules.product.enums.Status;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingRequest {
    @NotBlank(message = "Tên topping không được để trống")
    String name;

    @NotNull(message = "Giá topping là bắt buộc")
    @Min(value = 0)
    Double price;

    Status status;
}