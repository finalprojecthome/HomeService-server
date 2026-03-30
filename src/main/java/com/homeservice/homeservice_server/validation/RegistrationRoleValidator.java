package com.homeservice.homeservice_server.validation;

import com.homeservice.homeservice_server.enums.UserRole;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationRoleValidator implements ConstraintValidator<RegistrationRole, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank handles missing role
        }
        UserRole parsed = UserRole.tryFromDb(value.trim());
        if (parsed == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid role").addConstraintViolation();
            return false;
        }
        if (parsed == UserRole.ADMIN) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Admin role cannot be registered")
                    .addConstraintViolation();
            return false;
        }
        return parsed == UserRole.USER || parsed == UserRole.TECHNICIAN;
    }
}
