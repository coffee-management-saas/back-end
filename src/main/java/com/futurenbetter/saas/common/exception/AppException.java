package com.futurenbetter.saas.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }


    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}