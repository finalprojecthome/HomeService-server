package com.homeservice.homeservice_server.exception;

public class UnauthorizedException extends RuntimeException {

    /** Maps to HTTP 401 Unauthorized. */
    public UnauthorizedException(String message) {
        super(message);
    }
}
