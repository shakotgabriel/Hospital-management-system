package com.hospital.backend.config;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private final RateLimitingConfig rateLimitingConfig;

	public RateLimitingFilter(RateLimitingConfig rateLimitingConfig) {
		this.rateLimitingConfig = rateLimitingConfig;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String requestUri = request.getRequestURI();
		if (shouldSkipRateLimiting(requestUri)) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientIp = getClientIp(request);

		if (rateLimitingConfig.allowRequest(clientIp)) {
			filterChain.doFilter(request, response);
		} else {
			response.setStatus(429);
			response.setContentType("application/json");
			response.getWriter().write(
				"{\"status\": 429, \"error\": \"Too Many Requests\", " +
				"\"message\": \"Rate limit exceeded. Maximum 100 requests per minute.\"}"
			);
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0];
		}
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}
		return request.getRemoteAddr();
	}

	private boolean shouldSkipRateLimiting(String uri) {
		return uri.startsWith("/swagger-ui") ||
		       uri.startsWith("/v3/api-docs") ||
		       uri.startsWith("/actuator") ||
		       uri.startsWith("/static/");
	}
}
