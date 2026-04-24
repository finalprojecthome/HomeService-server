package com.homeservice.homeservice_server.config;

import com.homeservice.homeservice_server.config.admin.AdminAuthProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({SupabaseProperties.class, AdminAuthProperties.class})
public class SecurityConfig {
	@Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
	private String allowedOrigins;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				Arrays.stream(allowedOrigins.split(","))
						.map(String::trim)
						.filter(origin -> !origin.isEmpty())
						.toList()
		);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, SupabaseAuthenticationFilter supabaseAuthenticationFilter) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.httpBasic(basic -> basic.disable())
				.formLogin(form -> form.disable())
				.logout(logout -> logout.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> response.sendError(SC_UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/admin/auth/login", "/api/admin/auth/register").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().permitAll()
				)
				.addFilterBefore(supabaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
