package com.homeservice.homeservice_server.config;

import com.homeservice.homeservice_server.dto.supabase.SupabaseGetUserResponse;
import com.homeservice.homeservice_server.entities.User;
import com.homeservice.homeservice_server.repositories.UserRepository;
import com.homeservice.homeservice_server.services.SupabaseAuthClient;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SupabaseAuthenticationFilter extends OncePerRequestFilter {
	private final SupabaseAuthClient supabaseAuthClient;
	private final UserRepository userRepository;

	public SupabaseAuthenticationFilter(SupabaseAuthClient supabaseAuthClient, UserRepository userRepository) {
		this.supabaseAuthClient = supabaseAuthClient;
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
			SupabaseGetUserResponse supabaseUser = supabaseAuthClient.getUser(token);
			if (supabaseUser.id() == null || supabaseUser.email() == null || supabaseUser.email().isBlank()) {
				filterChain.doFilter(request, response);
				return;
			}

			UUID userId = UUID.fromString(supabaseUser.id());
			User user = userRepository.findById(userId).orElse(null);
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			if (user != null && user.getRole() != null) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
			}

			var auth = new UsernamePasswordAuthenticationToken(
					new SupabaseUserPrincipal(userId, supabaseUser.email()),
					null,
					authorities
			);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception ignored) {
			// Invalid token -> treat as unauthenticated
		}

		filterChain.doFilter(request, response);
	}
}
