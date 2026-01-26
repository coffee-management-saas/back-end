package com.futurenbetter.saas.modules.product.dto.response;

import com.futurenbetter.saas.modules.product.enums.Status;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    Long id;
    String name;
    LocalDateTime createdAt;
    Status status;
}