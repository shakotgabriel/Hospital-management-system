package com.hospital.backend;

import com.hospital.backend.entity.User;
import com.hospital.backend.entity.UserStatus;
import com.hospital.backend.repository.DoctorRepository;
import com.hospital.backend.repository.UserRepository;
import com.hospital.backend.service.DoctorService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DoctorServiceIntegrationTest {

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		doctorRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void adminShouldCreateAndUpdateDoctor() {
		User user = createUser("doctor.one@example.com", "Doctor", "One", "+14151110001");

		DoctorService.AdminCreateDoctorRequest createRequest = new DoctorService.AdminCreateDoctorRequest(
			user.getId(),
			"LIC-1001",
			"Cardiology",
			"Heart",
			new BigDecimal("50.00"),
			true
		);

		DoctorService.DoctorResponse created = doctorService.createDoctor(createRequest);
		assertNotNull(created.id());
		assertEquals("LIC-1001", created.licenseNumber());
		assertEquals(user.getId(), created.userId());

		DoctorService.AdminUpdateDoctorRequest updateRequest = new DoctorService.AdminUpdateDoctorRequest(
			null,
			"LIC-2002",
			"Neurology",
			"Neuro",
			new BigDecimal("75.00"),
			false
		);

		DoctorService.DoctorResponse updated = doctorService.updateDoctor(created.id(), updateRequest);
		assertEquals("LIC-2002", updated.licenseNumber());
		assertEquals("Neurology", updated.specialization());
		assertEquals("Neuro", updated.department());
		assertEquals(new BigDecimal("75.00"), updated.consultationFee());
		assertEquals(false, updated.available());
	}

	@Test
	void userShouldUpsertAndFetchOwnDoctorProfile() {
		User user = createUser("self.doctor@example.com", "Self", "Doctor", "+14151110002");

		DoctorService.UpsertMyDoctorProfileRequest upsertRequest = new DoctorService.UpsertMyDoctorProfileRequest(
			"LIC-3003",
			"General Medicine",
			"General",
			new BigDecimal("40.00"),
			true
		);

		DoctorService.DoctorResponse saved = doctorService.upsertMyProfile(user.getEmail(), upsertRequest);
		assertNotNull(saved.id());

		DoctorService.DoctorResponse fetched = doctorService.getMyProfile(user.getEmail());
		assertEquals(saved.id(), fetched.id());
		assertEquals("General Medicine", fetched.specialization());
	}

	@Test
	void listDoctorsShouldReturnCreatedDoctors() {
		User userA = createUser("doc.a@example.com", "Doc", "A", "+14151110003");
		User userB = createUser("doc.b@example.com", "Doc", "B", "+14151110004");

		doctorService.createDoctor(new DoctorService.AdminCreateDoctorRequest(
			userA.getId(),
			"LIC-4004",
			"Pediatrics",
			null,
			new BigDecimal("60.00"),
			true
		));
		doctorService.createDoctor(new DoctorService.AdminCreateDoctorRequest(
			userB.getId(),
			"LIC-5005",
			"Dermatology",
			null,
			new BigDecimal("65.00"),
			true
		));

		DoctorService.PagedDoctorResponse result = doctorService.listDoctors(0, 20);
		assertEquals(2, result.items().size());
	}

	@Test
	void shouldRejectDuplicateDoctorProfileForSameUser() {
		User user = createUser("dup.doctor@example.com", "Dup", "Doctor", "+14151110005");

		doctorService.createDoctor(new DoctorService.AdminCreateDoctorRequest(
			user.getId(),
			"LIC-6006",
			"Orthopedics",
			null,
			new BigDecimal("70.00"),
			true
		));

		assertThrows(IllegalArgumentException.class, () -> doctorService.createDoctor(
			new DoctorService.AdminCreateDoctorRequest(
				user.getId(),
				"LIC-7007",
				"Orthopedics",
				null,
				new BigDecimal("70.00"),
				true
			)
		));
	}

	@Test
	void shouldRejectDuplicateLicenseNumber() {
		User userA = createUser("lic.a@example.com", "Lic", "A", "+14151110006");
		User userB = createUser("lic.b@example.com", "Lic", "B", "+14151110007");

		doctorService.createDoctor(new DoctorService.AdminCreateDoctorRequest(
			userA.getId(),
			"LIC-8888",
			"Radiology",
			null,
			new BigDecimal("55.00"),
			true
		));

		assertThrows(IllegalArgumentException.class, () -> doctorService.createDoctor(
			new DoctorService.AdminCreateDoctorRequest(
				userB.getId(),
				"LIC-8888",
				"Pathology",
				null,
				new BigDecimal("55.00"),
				true
			)
		));
	}

	private User createUser(String email, String firstName, String lastName, String phone) {
		User user = new User();
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPhone(phone);
		user.setStatus(UserStatus.ACTIVE);
		user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
		return userRepository.save(user);
	}
}
