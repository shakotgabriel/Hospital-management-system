package com.hospital.backend.config;

import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UserRepository userRepository;

	public JwtAuthFilter(JwtUtils jwtUtils, UserRepository userRepository) {
		this.jwtUtils = jwtUtils;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);
		try {
			String email = jwtUtils.extractUsername(token);

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				User user = userRepository.findByEmail(email).orElse(null);
				if (user != null && user.getStatus() == UserStatus.ACTIVE && jwtUtils.isTokenValid(token, email)) {
					List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
						.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
						.toList();

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						email,
						null,
						authorities
					);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch (RuntimeException ignored) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
