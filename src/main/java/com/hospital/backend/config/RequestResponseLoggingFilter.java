package com.hospital.backend.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String requestUri = request.getRequestURI();
		if (shouldSkipLogging(requestUri)) {
			filterChain.doFilter(request, response);
			return;
		}

		long startTime = System.currentTimeMillis();
		String timestamp = LocalDateTime.now().format(formatter);
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String clientIp = getClientIp(request);

		try {
			filterChain.doFilter(request, response);
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			int status = response.getStatus();

			StringBuilder logMessage = new StringBuilder();
			logMessage.append(String.format(
				"[%s] %s %s | IP: %s | Status: %d | Duration: %dms",
				timestamp, method, uri, clientIp, status, duration
			));

			if (isErrorStatus(status)) {
				logger.warn(logMessage.toString());
			} else {
				logger.info(logMessage.toString());
			}
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

	private boolean isErrorStatus(int status) {
		return status >= 400;
	}

	private boolean shouldSkipLogging(String uri) {

		return uri.startsWith("/swagger-ui") ||
		       uri.startsWith("/v3/api-docs") ||
		       uri.startsWith("/actuator/health") ||
		       uri.startsWith("/static/") ||
		       uri.endsWith(".css") ||
		       uri.endsWith(".js") ||
		       uri.endsWith(".png") ||
		       uri.endsWith(".jpg");
	}
}
