package com.homeservice.homeservice_server.config;

import com.homeservice.homeservice_server.entity.User;
import com.homeservice.homeservice_server.entity.UserRole;
import com.homeservice.homeservice_server.repository.UserRepository;
import com.homeservice.homeservice_server.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring("Bearer ".length()).trim();
		if (token.isBlank() || SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Claims claims = jwtService.parseAndValidate(token);
			String email = jwtService.extractEmail(claims);
			UserRole role = jwtService.extractRole(claims);

			if (email == null || role == null) {
				filterChain.doFilter(request, response);
				return;
			}

			User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
			if (user == null || user.getRole() != role) {
				filterChain.doFilter(request, response);
				return;
			}

			var auth = new UsernamePasswordAuthenticationToken(
					email,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
			);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception ignored) {
			// Invalid token -> treat as unauthenticated
		}

		filterChain.doFilter(request, response);
	}
}
