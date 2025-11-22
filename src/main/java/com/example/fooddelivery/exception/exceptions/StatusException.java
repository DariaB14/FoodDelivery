package com.example.fooddelivery.exception.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StatusException extends RuntimeException {
    public StatusException(String message) {
        super(message);
    }
}
