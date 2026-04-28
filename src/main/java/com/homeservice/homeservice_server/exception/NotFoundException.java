package com.homeservice.homeservice_server.exception;

public class NotFoundException extends RuntimeException {

    /** Maps to HTTP 404 Not Found. */
    public NotFoundException(String message) {
        super(message);
    }
}
