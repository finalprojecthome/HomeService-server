package com.homeservice.homeservice_server.exception;

public class BadRequestException extends RuntimeException {

    /** Maps to HTTP 400 Bad Request. */
    public BadRequestException(String message) {
        super(message);
    }
}
