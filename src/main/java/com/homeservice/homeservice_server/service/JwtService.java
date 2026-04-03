package com.homeservice.homeservice_server.service;

import com.homeservice.homeservice_server.config.JwtProperties;
import com.homeservice.homeservice_server.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
	private static final String ROLE_CLAIM = "role";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = buildKey(jwtProperties.secret());
	}

	public String generateAccessToken(String email, UserRole role) {
		Instant now = Instant.now();
		Instant exp = now.plusMillis(jwtProperties.expiration());

		return Jwts.builder()
				.subject(email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claims(Map.of(ROLE_CLAIM, role.name()))
				.signWith(secretKey)
				.compact();
	}

	public Claims parseAndValidate(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String extractEmail(Claims claims) {
		return claims.getSubject();
	}

	public UserRole extractRole(Claims claims) {
		Object raw = claims.get(ROLE_CLAIM);
		if (raw == null) {
			return null;
		}
		return UserRole.valueOf(raw.toString());
	}

	private static SecretKey buildKey(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("jwt.secret is required");
		}

		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
		} catch (IllegalArgumentException ignored) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}

		if (keyBytes.length < 32) {
			throw new IllegalStateException("jwt.secret must be at least 256 bits (32 bytes)");
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
