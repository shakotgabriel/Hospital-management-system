package com.hospital.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/**",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				)
				.permitAll()
				.requestMatchers("/api/reception/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST")
				.requestMatchers("/api/cashier/**")
				.hasAnyRole("ADMIN", "CASHIER", "RECEPTIONIST")
				.requestMatchers("/api/admin/patients/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST")
				.requestMatchers("/api/admin/doctors/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST")
				.requestMatchers("/api/admin/appointments/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST")
				.requestMatchers("/api/admin/bills/**")
				.hasAnyRole("ADMIN", "CASHIER", "RECEPTIONIST")
				.requestMatchers("/api/admin/prescriptions/**")
				.hasRole("ADMIN")
				.requestMatchers("/api/admin/**")
				.hasRole("ADMIN")
				.requestMatchers("/api/users/**")
				.authenticated()
				.anyRequest()
				.authenticated())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
