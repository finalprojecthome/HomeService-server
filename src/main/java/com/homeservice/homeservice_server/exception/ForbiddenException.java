package com.homeservice.homeservice_server.exception;

public class ForbiddenException extends RuntimeException {

    /** Maps to HTTP 403 Forbidden. */
    public ForbiddenException(String message) {
        super(message);
    }
}
