package com.hospital.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

	private static final long DEFAULT_EXPIRATION_MILLIS = 24 * 60 * 60 * 1000;

	private final Key signingKey;
	private final long expirationMillis;

	public JwtUtils(
		@Value("${app.jwt.secret}") String jwtSecret,
		@Value("${app.jwt.expiration-ms:" + DEFAULT_EXPIRATION_MILLIS + "}") long expirationMillis
	) {
		try {
			this.signingKey = Keys.hmacShaKeyFor(resolveSecretBytes(jwtSecret));
		} catch (Exception ex) {
			throw new IllegalStateException(
				"Invalid app.jwt.secret. Use Base64/Base64URL or plain text with at least 32 characters.",
				ex
			);
		}
		this.expirationMillis = expirationMillis;
	}

	private byte[] resolveSecretBytes(String jwtSecret) {
		String secret = jwtSecret == null ? "" : jwtSecret.trim();
		if (secret.isEmpty()) {
			throw new IllegalStateException("app.jwt.secret is empty");
		}

		try {
			return Decoders.BASE64.decode(secret);
		} catch (Exception ignored) {
		}

		try {
			return Decoders.BASE64URL.decode(secret);
		} catch (Exception ignored) {
		}

		return secret.getBytes(StandardCharsets.UTF_8);
	}

	public String generateToken(String subject) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMillis);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, String expectedSubject) {
		Claims claims = extractClaims(token);
		return expectedSubject.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
	}

	private Claims extractClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
