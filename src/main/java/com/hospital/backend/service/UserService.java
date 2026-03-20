package com.hospital.backend.service;

import com.hospital.backend.entity.Role;
import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.RoleRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserResponse getCurrentUser(String email) {
		User user = findByEmailOrThrow(email);
		return toUserResponse(user);
	}

	@Transactional
	public UserResponse updateCurrentUser(String email, UpdateProfileRequest request) {
		User user = findByEmailOrThrow(email);

		if (request.firstName() != null && !request.firstName().isBlank()) {
			user.setFirstName(request.firstName().trim());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			user.setLastName(request.lastName().trim());
		}
		if (request.phone() != null) {
			String normalizedPhone = normalizePhone(request.phone());
			if (normalizedPhone != null && !normalizedPhone.equals(user.getPhone()) && userRepository.existsByPhone(normalizedPhone)) {
				throw new IllegalArgumentException("Phone is already in use");
			}
			user.setPhone(normalizedPhone);
		}

		User saved = userRepository.save(user);
		return toUserResponse(saved);
	}

	public List<UserResponse> listUsers() {
		return userRepository.findAll().stream()
			.sorted(Comparator.comparing(User::getId))
			.map(this::toUserResponse)
			.toList();
	}

	@Transactional
	public UserResponse createUser(AdminCreateUserRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}

		String phone = normalizePhone(request.phone());
		if (phone != null && userRepository.existsByPhone(phone)) {
			throw new IllegalArgumentException("Phone is already in use");
		}

		Set<Role> roles = resolveRoles(request.roles());

		User user = new User();
		user.setEmail(email);
		user.setPhone(phone);
		user.setFirstName(request.firstName().trim());
		user.setLastName(request.lastName().trim());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setStatus(request.status() == null ? UserStatus.ACTIVE : request.status());
		user.setRoles(roles);

		User saved = userRepository.save(user);
		return toUserResponse(saved);
	}

	@Transactional
	public UserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		if (request.firstName() != null && !request.firstName().isBlank()) {
			user.setFirstName(request.firstName().trim());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			user.setLastName(request.lastName().trim());
		}
		if (request.phone() != null) {
			String normalizedPhone = normalizePhone(request.phone());
			if (normalizedPhone != null && !normalizedPhone.equals(user.getPhone()) && userRepository.existsByPhone(normalizedPhone)) {
				throw new IllegalArgumentException("Phone is already in use");
			}
			user.setPhone(normalizedPhone);
		}
		if (request.status() != null) {
			user.setStatus(request.status());
		}
		if (request.password() != null && !request.password().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		if (request.roles() != null && !request.roles().isEmpty()) {
			user.setRoles(resolveRoles(request.roles()));
		}

		User saved = userRepository.save(user);
		return toUserResponse(saved);
	}

	@Transactional
	public UserResponse setUserActiveStatus(Long userId, boolean active) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
		User saved = userRepository.save(user);
		return toUserResponse(saved);
	}

	private User findByEmailOrThrow(String email) {
		String normalizedEmail = normalizeEmail(email);
		return userRepository.findByEmail(normalizedEmail)
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + normalizedEmail));
	}

	private Set<Role> resolveRoles(Set<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty()) {
			throw new IllegalArgumentException("At least one role is required");
		}

		Set<String> normalizedRoleNames = roleNames.stream()
			.filter(role -> role != null && !role.isBlank())
			.map(role -> role.trim().toUpperCase(Locale.ROOT))
			.collect(Collectors.toSet());

		if (normalizedRoleNames.isEmpty()) {
			throw new IllegalArgumentException("At least one valid role is required");
		}

		return normalizedRoleNames.stream()
			.map(roleName -> roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
			.collect(Collectors.toSet());
	}

	private UserResponse toUserResponse(User user) {
		Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getPhone(),
			user.getFirstName(),
			user.getLastName(),
			user.getStatus(),
			roles,
			user.getLastLoginAt(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}

	private String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizePhone(String phone) {
		if (phone == null) {
			return null;
		}
		String normalized = phone.trim();
		return normalized.isBlank() ? null : normalized;
	}

	public record UpdateProfileRequest(String firstName, String lastName, String phone) {
	}

	public record AdminCreateUserRequest(
		String email,
		String phone,
		String password,
		String firstName,
		String lastName,
		Set<String> roles,
		UserStatus status
	) {
	}

	public record AdminUpdateUserRequest(
		String phone,
		String password,
		String firstName,
		String lastName,
		Set<String> roles,
		UserStatus status
	) {
	}

	public record UserResponse(
		Long id,
		String email,
		String phone,
		String firstName,
		String lastName,
		UserStatus status,
		Set<String> roles,
		OffsetDateTime lastLoginAt,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
	}
}
