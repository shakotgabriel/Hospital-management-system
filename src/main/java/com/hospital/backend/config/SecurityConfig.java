package com.hospital.backend.config;

import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.UserRepository;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/auth/login",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				)
				.permitAll()
				.requestMatchers("/api/auth/**")
				.denyAll()
				.requestMatchers("/api/admin/**")
				.hasRole("ADMIN")
				.requestMatchers("/api/reception/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST")
				.requestMatchers("/api/cashier/**")
				.hasAnyRole("ADMIN", "CASHIER")
				.requestMatchers("/api/doctors/**")
				.hasAnyRole("ADMIN", "DOCTOR")
				.requestMatchers("/api/patients/me/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST", "CASHIER")
				.requestMatchers("/api/appointments/**")
				.hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST", "CASHIER")
				.requestMatchers("/api/bills/me/**")
				.hasAnyRole("ADMIN", "RECEPTIONIST", "CASHIER")
				.requestMatchers("/api/patients/**", "/api/bills/**")
				.denyAll()
				.requestMatchers("/api/users/me")
				.hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST", "CASHIER")
				.requestMatchers("/api/users/**")
				.hasRole("ADMIN")
				.anyRequest()
				.authenticated())
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return username -> {
			String normalizedEmail = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
			User user = userRepository.findByEmail(normalizedEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalizedEmail));

			boolean enabled = user.getStatus() == UserStatus.ACTIVE;
			return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPasswordHash(),
				enabled,
				true,
				true,
				true,
				user.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase(Locale.ROOT)))
					.toList()
			);
		};
	}

	@Bean
	public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
