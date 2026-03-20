package com.hospital.backend.config;

import com.hospital.backend.entity.Role;
import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.RoleRepository;
import com.hospital.backend.repository.UserRepository;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner seedRolesAndAdmin(
		RoleRepository roleRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		@Value("${app.admin.email}") String adminEmail,
		@Value("${app.admin.password}") String adminPassword,
		@Value("${app.admin.first-name}") String adminFirstName,
		@Value("${app.admin.last-name}") String adminLastName
	) {
		return args -> {
			Role adminRole = ensureRole(roleRepository, "ADMIN", "Full platform access");
			ensureRole(roleRepository, "DOCTOR", "Doctor access");
			ensureRole(roleRepository, "PATIENT", "Patient access");

			String normalizedAdminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
			if (userRepository.existsByEmail(normalizedAdminEmail)) {
				return;
			}

			User admin = new User();
			admin.setEmail(normalizedAdminEmail);
			admin.setPasswordHash(passwordEncoder.encode(adminPassword));
			admin.setFirstName(adminFirstName.trim());
			admin.setLastName(adminLastName.trim());
			admin.setStatus(UserStatus.ACTIVE);
			admin.setRoles(Set.of(adminRole));
			userRepository.save(admin);
		};
	}

	private Role ensureRole(RoleRepository roleRepository, String roleName, String description) {
		return roleRepository.findByName(roleName).orElseGet(() -> {
			Role role = new Role();
			role.setName(roleName);
			role.setDescription(description);
			return roleRepository.save(role);
		});
	}
}
