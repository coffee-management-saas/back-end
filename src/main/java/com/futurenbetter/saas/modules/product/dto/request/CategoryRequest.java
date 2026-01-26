package com.futurenbetter.saas.modules.product.dto.request;

import com.futurenbetter.saas.modules.product.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    String name;
    LocalDateTime createdAt;
    Status status;
}