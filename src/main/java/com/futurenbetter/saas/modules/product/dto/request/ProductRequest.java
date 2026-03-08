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
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    String name;

    @NotNull(message = "Danh mục là bắt buộc")
    Long categoryId;

    @Min(value = 0, message = "Giá sản phẩm không được âm")
    Long price;
    String description;
    String image;
    Status status;
}