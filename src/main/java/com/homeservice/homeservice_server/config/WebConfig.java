package com.homeservice.homeservice_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.homeservice.homeservice_server.security.AuthGuardInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final AuthGuardInterceptor authGuardInterceptor;

    public WebConfig(CorsProperties corsProperties, AuthGuardInterceptor authGuardInterceptor) {
        this.corsProperties = corsProperties;
        this.authGuardInterceptor = authGuardInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsProperties.allowedOriginArray();
        if (origins.length == 0) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authGuardInterceptor).addPathPatterns("/**");
    }
}
