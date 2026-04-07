package com.homeservice.homeservice_server.security;

import java.lang.annotation.Annotation;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.homeservice.homeservice_server.dto.auth.GetUserResponse;
import com.homeservice.homeservice_server.enums.UserRole;
import com.homeservice.homeservice_server.exception.ForbiddenException;
import com.homeservice.homeservice_server.exception.UnauthorizedException;
import com.homeservice.homeservice_server.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthGuardInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthGuardInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiredAccess requiredAccess = resolveRequiredAccess(handlerMethod);
        if (requiredAccess == null) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new UnauthorizedException("กรุณาเข้าสู่ระบบก่อนใช้งาน");
        }

        String accessToken = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        if (accessToken.isBlank()) {
            throw new UnauthorizedException("โทเคนไม่ถูกต้อง");
        }

        GetUserResponse currentUser = authService.getUser(accessToken);
        UserRole currentRole = currentUser.getRole();
        UUID currentUserId = UUID.fromString(currentUser.getId());

        request.setAttribute(RequestUserContext.ATTR_USER_ID, currentUserId);
        request.setAttribute(RequestUserContext.ATTR_USER_ROLE, currentRole);

        if (requiredAccess.role != null && currentRole != requiredAccess.role) {
            throw new ForbiddenException("คุณไม่มีสิทธิ์เข้าถึงข้อมูลนี้");
        }
        return true;
    }

    private static RequiredAccess resolveRequiredAccess(HandlerMethod handlerMethod) {
        if (hasAnnotation(handlerMethod, AdminOnly.class)) {
            return new RequiredAccess(UserRole.ADMIN);
        }
        if (hasAnnotation(handlerMethod, TechnicianOnly.class)) {
            return new RequiredAccess(UserRole.TECHNICIAN);
        }
        if (hasAnnotation(handlerMethod, UserOnly.class)) {
            return new RequiredAccess(UserRole.USER);
        }
        if (hasAnnotation(handlerMethod, AuthRequired.class)) {
            return new RequiredAccess(null);
        }
        return null;
    }

    private static boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotationClass) {
        return handlerMethod.hasMethodAnnotation(annotationClass)
                || handlerMethod.getBeanType().isAnnotationPresent(annotationClass);
    }

    private static final class RequiredAccess {
        private final UserRole role;

        private RequiredAccess(UserRole role) {
            this.role = role;
        }
    }
}
