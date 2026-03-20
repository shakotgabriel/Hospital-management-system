package com.hospital.backend.service;

import com.hospital.backend.entity.Patient;
import com.hospital.backend.entity.User;
import com.hospital.backend.repository.PatientRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

	private static final DateTimeFormatter PATIENT_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final PatientRepository patientRepository;
	private final UserRepository userRepository;

	public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
		this.patientRepository = patientRepository;
		this.userRepository = userRepository;
	}

	public PatientResponse getMyProfile(String email) {
		User user = findUserByEmail(email);
		Patient patient = patientRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Patient profile not found for current user"));
		return toResponse(patient);
	}

	@Transactional
	public PatientResponse upsertMyProfile(String email, UpsertMyPatientProfileRequest request) {
		User user = findUserByEmail(email);
		Patient patient = patientRepository.findByUserId(user.getId()).orElseGet(Patient::new);

		if (patient.getId() == null) {
			patient.setUser(user);
			patient.setPatientNumber(generatePatientNumber());
		}

		applyCoreFields(patient, request.firstName(), request.lastName(), request.dateOfBirth());
		applyOptionalFields(
			patient,
			request.gender(),
			request.bloodGroup(),
			request.phone(),
			request.email(),
			request.addressLine1(),
			request.addressLine2(),
			request.city(),
			request.state(),
			request.country(),
			request.emergencyName(),
			request.emergencyPhone(),
			request.allergies(),
			request.chronicDiseases()
		);

		Patient saved = patientRepository.save(patient);
		return toResponse(saved);
	}

	public PagedPatientResponse listPatients(String search, int page, int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		Page<Patient> patientPage;

		if (search == null || search.isBlank()) {
			patientPage = patientRepository.findAll(pageable);
		} else {
			String query = search.trim();
			patientPage = patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
				query,
				query,
				pageable
			);
		}

		return new PagedPatientResponse(
			patientPage.getContent().stream().map(this::toResponse).toList(),
			patientPage.getNumber(),
			patientPage.getSize(),
			patientPage.getTotalElements(),
			patientPage.getTotalPages()
		);
	}

	public PatientResponse getPatientById(Long patientId) {
		Patient patient = patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
		return toResponse(patient);
	}

	@Transactional
	public PatientResponse createPatient(AdminCreatePatientRequest request) {
		Patient patient = new Patient();
		patient.setPatientNumber(generatePatientNumber());

		if (request.userId() != null) {
			User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
			patientRepository.findByUserId(user.getId()).ifPresent(existing -> {
				throw new IllegalArgumentException("Patient profile already exists for user: " + user.getId());
			});
			patient.setUser(user);
		}

		applyCoreFields(patient, request.firstName(), request.lastName(), request.dateOfBirth());
		applyOptionalFields(
			patient,
			request.gender(),
			request.bloodGroup(),
			request.phone(),
			request.email(),
			request.addressLine1(),
			request.addressLine2(),
			request.city(),
			request.state(),
			request.country(),
			request.emergencyName(),
			request.emergencyPhone(),
			request.allergies(),
			request.chronicDiseases()
		);

		Patient saved = patientRepository.save(patient);
		return toResponse(saved);
	}

	@Transactional
	public PatientResponse updatePatient(Long patientId, AdminUpdatePatientRequest request) {
		Patient patient = patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

		if (request.userId() != null) {
			User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
			patientRepository.findByUserId(user.getId()).ifPresent(existing -> {
				if (!existing.getId().equals(patient.getId())) {
					throw new IllegalArgumentException("Patient profile already exists for user: " + user.getId());
				}
			});
			patient.setUser(user);
		}

		if (request.firstName() != null && !request.firstName().isBlank()) {
			patient.setFirstName(request.firstName().trim());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			patient.setLastName(request.lastName().trim());
		}
		if (request.dateOfBirth() != null) {
			patient.setDateOfBirth(request.dateOfBirth());
		}

		applyOptionalFields(
			patient,
			request.gender(),
			request.bloodGroup(),
			request.phone(),
			request.email(),
			request.addressLine1(),
			request.addressLine2(),
			request.city(),
			request.state(),
			request.country(),
			request.emergencyName(),
			request.emergencyPhone(),
			request.allergies(),
			request.chronicDiseases()
		);

		Patient saved = patientRepository.save(patient);
		return toResponse(saved);
	}

	private User findUserByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
	}

	private void applyCoreFields(Patient patient, String firstName, String lastName, LocalDate dateOfBirth) {
		if (firstName == null || firstName.isBlank()) {
			throw new IllegalArgumentException("First name is required");
		}
		if (lastName == null || lastName.isBlank()) {
			throw new IllegalArgumentException("Last name is required");
		}
		if (dateOfBirth == null) {
			throw new IllegalArgumentException("Date of birth is required");
		}

		patient.setFirstName(firstName.trim());
		patient.setLastName(lastName.trim());
		patient.setDateOfBirth(dateOfBirth);
	}

	private void applyOptionalFields(
		Patient patient,
		String gender,
		String bloodGroup,
		String phone,
		String email,
		String addressLine1,
		String addressLine2,
		String city,
		String state,
		String country,
		String emergencyName,
		String emergencyPhone,
		String allergies,
		String chronicDiseases
	) {
		if (gender != null) {
			patient.setGender(trimToNull(gender));
		}
		if (bloodGroup != null) {
			patient.setBloodGroup(trimToNull(bloodGroup));
		}
		if (phone != null) {
			patient.setPhone(trimToNull(phone));
		}
		if (email != null) {
			patient.setEmail(trimToNull(email));
		}
		if (addressLine1 != null) {
			patient.setAddressLine1(trimToNull(addressLine1));
		}
		if (addressLine2 != null) {
			patient.setAddressLine2(trimToNull(addressLine2));
		}
		if (city != null) {
			patient.setCity(trimToNull(city));
		}
		if (state != null) {
			patient.setState(trimToNull(state));
		}
		if (country != null) {
			patient.setCountry(trimToNull(country));
		}
		if (emergencyName != null) {
			patient.setEmergencyName(trimToNull(emergencyName));
		}
		if (emergencyPhone != null) {
			patient.setEmergencyPhone(trimToNull(emergencyPhone));
		}
		if (allergies != null) {
			patient.setAllergies(trimToNull(allergies));
		}
		if (chronicDiseases != null) {
			patient.setChronicDiseases(trimToNull(chronicDiseases));
		}
	}

	private String trimToNull(String value) {
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String generatePatientNumber() {
		for (int i = 0; i < 10; i++) {
			String candidate = "PAT-" + LocalDate.now().format(PATIENT_NUMBER_DATE) + "-"
				+ UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
			if (patientRepository.findByPatientNumber(candidate).isEmpty()) {
				return candidate;
			}
		}
		throw new IllegalStateException("Failed to generate unique patient number");
	}

	private PatientResponse toResponse(Patient patient) {
		Long linkedUserId = patient.getUser() != null ? patient.getUser().getId() : null;
		return new PatientResponse(
			patient.getId(),
			linkedUserId,
			patient.getPatientNumber(),
			patient.getFirstName(),
			patient.getLastName(),
			patient.getDateOfBirth(),
			patient.getGender(),
			patient.getBloodGroup(),
			patient.getPhone(),
			patient.getEmail(),
			patient.getAddressLine1(),
			patient.getAddressLine2(),
			patient.getCity(),
			patient.getState(),
			patient.getCountry(),
			patient.getEmergencyName(),
			patient.getEmergencyPhone(),
			patient.getAllergies(),
			patient.getChronicDiseases(),
			patient.getCreatedAt(),
			patient.getUpdatedAt()
		);
	}

	public record UpsertMyPatientProfileRequest(
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String gender,
		String bloodGroup,
		String phone,
		String email,
		String addressLine1,
		String addressLine2,
		String city,
		String state,
		String country,
		String emergencyName,
		String emergencyPhone,
		String allergies,
		String chronicDiseases
	) {
	}

	public record AdminCreatePatientRequest(
		Long userId,
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String gender,
		String bloodGroup,
		String phone,
		String email,
		String addressLine1,
		String addressLine2,
		String city,
		String state,
		String country,
		String emergencyName,
		String emergencyPhone,
		String allergies,
		String chronicDiseases
	) {
	}

	public record AdminUpdatePatientRequest(
		Long userId,
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String gender,
		String bloodGroup,
		String phone,
		String email,
		String addressLine1,
		String addressLine2,
		String city,
		String state,
		String country,
		String emergencyName,
		String emergencyPhone,
		String allergies,
		String chronicDiseases
	) {
	}

	public record PagedPatientResponse(
		java.util.List<PatientResponse> items,
		int page,
		int size,
		long totalItems,
		int totalPages
	) {
	}

	public record PatientResponse(
		Long id,
		Long userId,
		String patientNumber,
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		String gender,
		String bloodGroup,
		String phone,
		String email,
		String addressLine1,
		String addressLine2,
		String city,
		String state,
		String country,
		String emergencyName,
		String emergencyPhone,
		String allergies,
		String chronicDiseases,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
	}
}
