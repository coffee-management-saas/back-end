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
public class ProductVariantRequest {
    @NotNull(message = "Sản phẩm là bắt buộc")
    Long productId;

    @NotNull(message = "Kích thước là bắt buộc")
    Long sizeId;

    @NotNull(message = "Giá bán là bắt buộc")
    @Min(value = 0)
    Double price;

    @Min(value = 0)
    Double costPrice; // Giá vốn

    @NotBlank(message = "SKU Code là bắt buộc")
    String skuCode;

    Status status;
}