package com.hospital.backend.service;

import com.hospital.backend.entity.Doctor;
import com.hospital.backend.entity.User;
import com.hospital.backend.repository.DoctorRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {

	private final DoctorRepository doctorRepository;
	private final UserRepository userRepository;

	public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository) {
		this.doctorRepository = doctorRepository;
		this.userRepository = userRepository;
	}

	public DoctorResponse getMyProfile(String email) {
		User user = findUserByEmail(email);
		Doctor doctor = doctorRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Doctor profile not found for current user"));
		return toResponse(doctor);
	}

	@Transactional
	public DoctorResponse upsertMyProfile(String email, UpsertMyDoctorProfileRequest request) {
		User user = findUserByEmail(email);
		Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseGet(Doctor::new);

		if (doctor.getId() == null) {
			doctor.setUser(user);
		}

		if (request.licenseNumber() == null || request.licenseNumber().isBlank()) {
			throw new IllegalArgumentException("License number is required");
		}
		String normalizedLicense = request.licenseNumber().trim().toUpperCase(Locale.ROOT);
		validateLicenseUniqueness(normalizedLicense, doctor.getId());

		if (request.specialization() == null || request.specialization().isBlank()) {
			throw new IllegalArgumentException("Specialization is required");
		}

		doctor.setLicenseNumber(normalizedLicense);
		doctor.setSpecialization(request.specialization().trim());
		doctor.setDepartment(trimToNull(request.department()));
		doctor.setConsultationFee(normalizeFee(request.consultationFee()));
		doctor.setAvailable(request.available() == null || request.available());

		Doctor saved = doctorRepository.save(doctor);
		return toResponse(saved);
	}

	public PagedDoctorResponse listDoctors(int page, int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		Page<Doctor> doctorPage = doctorRepository.findAll(pageable);
		return new PagedDoctorResponse(
			doctorPage.getContent().stream().map(this::toResponse).toList(),
			doctorPage.getNumber(),
			doctorPage.getSize(),
			doctorPage.getTotalElements(),
			doctorPage.getTotalPages()
		);
	}

	public DoctorResponse getDoctorById(Long doctorId) {
		Doctor doctor = doctorRepository.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
		return toResponse(doctor);
	}

	@Transactional
	public DoctorResponse createDoctor(AdminCreateDoctorRequest request) {
		if (request.userId() == null) {
			throw new IllegalArgumentException("User id is required");
		}
		User user = userRepository.findById(request.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
		doctorRepository.findByUserId(user.getId()).ifPresent(existing -> {
			throw new IllegalArgumentException("Doctor profile already exists for user: " + user.getId());
		});

		if (request.licenseNumber() == null || request.licenseNumber().isBlank()) {
			throw new IllegalArgumentException("License number is required");
		}
		if (request.specialization() == null || request.specialization().isBlank()) {
			throw new IllegalArgumentException("Specialization is required");
		}

		String normalizedLicense = request.licenseNumber().trim().toUpperCase(Locale.ROOT);
		validateLicenseUniqueness(normalizedLicense, null);

		Doctor doctor = new Doctor();
		doctor.setUser(user);
		doctor.setLicenseNumber(normalizedLicense);
		doctor.setSpecialization(request.specialization().trim());
		doctor.setDepartment(trimToNull(request.department()));
		doctor.setConsultationFee(normalizeFee(request.consultationFee()));
		doctor.setAvailable(request.available() == null || request.available());

		Doctor saved = doctorRepository.save(doctor);
		return toResponse(saved);
	}

	@Transactional
	public DoctorResponse updateDoctor(Long doctorId, AdminUpdateDoctorRequest request) {
		Doctor doctor = doctorRepository.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

		if (request.userId() != null) {
			User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
			doctorRepository.findByUserId(user.getId()).ifPresent(existing -> {
				if (!existing.getId().equals(doctor.getId())) {
					throw new IllegalArgumentException("Doctor profile already exists for user: " + user.getId());
				}
			});
			doctor.setUser(user);
		}

		if (request.licenseNumber() != null) {
			String normalizedLicense = request.licenseNumber().trim().toUpperCase(Locale.ROOT);
			if (normalizedLicense.isEmpty()) {
				throw new IllegalArgumentException("License number cannot be blank");
			}
			validateLicenseUniqueness(normalizedLicense, doctor.getId());
			doctor.setLicenseNumber(normalizedLicense);
		}

		if (request.specialization() != null) {
			if (request.specialization().isBlank()) {
				throw new IllegalArgumentException("Specialization cannot be blank");
			}
			doctor.setSpecialization(request.specialization().trim());
		}
		if (request.department() != null) {
			doctor.setDepartment(trimToNull(request.department()));
		}
		if (request.consultationFee() != null) {
			doctor.setConsultationFee(normalizeFee(request.consultationFee()));
		}
		if (request.available() != null) {
			doctor.setAvailable(request.available());
		}

		Doctor saved = doctorRepository.save(doctor);
		return toResponse(saved);
	}

	private User findUserByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
	}

	private void validateLicenseUniqueness(String licenseNumber, Long currentDoctorId) {
		doctorRepository.findByLicenseNumber(licenseNumber).ifPresent(existing -> {
			if (currentDoctorId == null || !existing.getId().equals(currentDoctorId)) {
				throw new IllegalArgumentException("License number already exists");
			}
		});
	}

	private BigDecimal normalizeFee(BigDecimal fee) {
		if (fee == null) {
			return BigDecimal.ZERO;
		}
		if (fee.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Consultation fee cannot be negative");
		}
		return fee;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private DoctorResponse toResponse(Doctor doctor) {
		return new DoctorResponse(
			doctor.getId(),
			doctor.getUser() != null ? doctor.getUser().getId() : null,
			doctor.getLicenseNumber(),
			doctor.getSpecialization(),
			doctor.getDepartment(),
			doctor.getConsultationFee(),
			doctor.isAvailable(),
			doctor.getCreatedAt(),
			doctor.getUpdatedAt()
		);
	}

	public record UpsertMyDoctorProfileRequest(
		String licenseNumber,
		String specialization,
		String department,
		BigDecimal consultationFee,
		Boolean available
	) {
	}

	public record AdminCreateDoctorRequest(
		Long userId,
		String licenseNumber,
		String specialization,
		String department,
		BigDecimal consultationFee,
		Boolean available
	) {
	}

	public record AdminUpdateDoctorRequest(
		Long userId,
		String licenseNumber,
		String specialization,
		String department,
		BigDecimal consultationFee,
		Boolean available
	) {
	}

	public record PagedDoctorResponse(
		List<DoctorResponse> items,
		int page,
		int size,
		long totalItems,
		int totalPages
	) {
	}

	public record DoctorResponse(
		Long id,
		Long userId,
		String licenseNumber,
		String specialization,
		String department,
		BigDecimal consultationFee,
		boolean available,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
	}
}
