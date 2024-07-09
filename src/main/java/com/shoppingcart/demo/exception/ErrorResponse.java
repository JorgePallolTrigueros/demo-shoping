package com.shoppingcart.demo.exception;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorResponse {

    private final String message;
    private final int code;

    private final Date timestamp;

    public ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
        this.timestamp = new Date();
    }
}
