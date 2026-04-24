package com.homeservice.homeservice_server.exception;

public class ConflictException extends RuntimeException {

    /** Maps to HTTP 409 Conflict. */
    public ConflictException(String message) {
        super(message);
    }
}
