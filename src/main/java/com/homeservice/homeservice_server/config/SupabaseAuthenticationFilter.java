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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates Supabase-issued JWTs by calling the Supabase /auth/v1/user endpoint.
 *
 * Results are cached in-memory for TOKEN_CACHE_TTL_MS (60 seconds) to avoid a
 * remote HTTP round-trip on every API request. After the first successful
 * validation of a token, subsequent requests using the same token return
 * immediately from the cache.
 */
@Component
public class SupabaseAuthenticationFilter extends OncePerRequestFilter {

	private static final long TOKEN_CACHE_TTL_MS = 60_000L;

	private record CachedAuth(SupabaseGetUserResponse user, long cachedAt) {
		boolean isExpired() {
			return System.currentTimeMillis() - cachedAt > TOKEN_CACHE_TTL_MS;
		}
	}

	private final ConcurrentHashMap<String, CachedAuth> tokenCache = new ConcurrentHashMap<>();

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
			SupabaseGetUserResponse supabaseUser = resolveUser(token);
			if (supabaseUser == null || supabaseUser.id() == null || supabaseUser.email() == null || supabaseUser.email().isBlank()) {
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
			// Invalid token → treat as unauthenticated
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Returns the Supabase user for the given token.
	 * The result is cached for TOKEN_CACHE_TTL_MS to avoid repeated HTTP calls.
	 * The first call per token per server-start will still contact Supabase once;
	 * every subsequent call within the TTL window is served instantly from cache.
	 */
	private SupabaseGetUserResponse resolveUser(String token) {
		CachedAuth cached = tokenCache.get(token);
		if (cached != null && !cached.isExpired()) {
			return cached.user();
		}

		tokenCache.remove(token);

		SupabaseGetUserResponse fresh = supabaseAuthClient.getUser(token);
		tokenCache.put(token, new CachedAuth(fresh, System.currentTimeMillis()));

		if (tokenCache.size() > 500) {
			tokenCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
		}

		return fresh;
	}
}
