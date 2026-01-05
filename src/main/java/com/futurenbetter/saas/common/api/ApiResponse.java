package com.futurenbetter.saas.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String status;
    private String message;
    private T data;
    private Object error;
    private PageMeta meta;

    public static <T> ApiResponse<T> success(
            HttpStatus httpStatus,
            String message,
            T data,
            PageMeta meta
    ) {
        return new ApiResponse<T>(
                httpStatus.value(),
                httpStatus.name(),
                message,
                data,
                null,
                meta
        );
    }

    public static <T> ApiResponse<T> error(
            HttpStatus httpStatus,
            String message,
            Object error
    ) {
        return new ApiResponse<T>(
                httpStatus.value(),
                httpStatus.name(),
                message,
                null,
                error,
                null
        );
    }
}
