package com.homeservice.homeservice_server.validation;

/**
 * Shared patterns for {@link jakarta.validation.constraints.Pattern} and
 * similar checks.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String FULL_NAME_PATTERN = "^[a-zA-Zก-๏\s]+$";

    public static final String PHONE_PATTERN = "^0\\d{9}$";
}
