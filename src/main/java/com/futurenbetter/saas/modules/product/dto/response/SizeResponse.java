package com.futurenbetter.saas.modules.product.dto.response;

import com.futurenbetter.saas.modules.product.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeResponse {

    Long sizeId;
    String code;
    Status status;

}
