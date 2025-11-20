package com.example.fooddelivery.exception.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
}
