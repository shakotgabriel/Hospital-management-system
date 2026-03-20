package com.hospital.backend.service;

import com.hospital.backend.config.JwtUtils;
import com.hospital.backend.dto.LoginRequest;
import com.hospital.backend.dto.SignupRequest;
import com.hospital.backend.entity.Role;
import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.RoleRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder passwordEncoder;

	public AuthService(
		UserRepository userRepository,
		RoleRepository roleRepository,
		JwtUtils jwtUtils,
		PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.jwtUtils = jwtUtils;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}

		String phone = normalizePhone(request.getPhone());
		if (phone != null && userRepository.existsByPhone(phone)) {
			throw new IllegalArgumentException("Phone is already in use");
		}

		String requestedRole = request.getRole().trim().toUpperCase(Locale.ROOT);
		Role role = roleRepository.findByName(requestedRole)
			.orElseThrow(() -> new IllegalArgumentException("Role not found: " + requestedRole));

		User user = new User();
		user.setEmail(email);
		user.setPhone(phone);
		user.setFirstName(request.getFirstName().trim());
		user.setLastName(request.getLastName().trim());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(role));

		User saved = userRepository.save(user);
		String token = jwtUtils.generateToken(saved.getEmail());

		return toAuthResponse(saved, token);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BadCredentialsException("User account is not active");
		}

		user.setLastLoginAt(OffsetDateTime.now());
		User updated = userRepository.save(user);
		String token = jwtUtils.generateToken(updated.getEmail());

		return toAuthResponse(updated, token);
	}

	private AuthResponse toAuthResponse(User user, String token) {
		Set<String> roles = user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
		return new AuthResponse(
			token,
			user.getId(),
			user.getEmail(),
			user.getFirstName(),
			user.getLastName(),
			roles
		);
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizePhone(String phone) {
		if (phone == null) {
			return null;
		}
		String normalized = phone.trim();
		return normalized.isBlank() ? null : normalized;
	}

	public record AuthResponse(
		String accessToken,
		Long userId,
		String email,
		String firstName,
		String lastName,
		Set<String> roles
	) {
	}
}
