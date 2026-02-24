package com.futurenbetter.saas.common.exception.handler;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiResponseExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ApiResponse handleAppException(Exception ex) {
        return ApiResponse.error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null);
    }
}
