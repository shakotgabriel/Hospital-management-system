package com.hospital.backend.service;

import com.hospital.backend.entity.Appointment;
import com.hospital.backend.entity.Doctor;
import com.hospital.backend.entity.Prescription;
import com.hospital.backend.entity.User;
import com.hospital.backend.repository.AppointmentRepository;
import com.hospital.backend.repository.DoctorRepository;
import com.hospital.backend.repository.PrescriptionRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionService {

	private final PrescriptionRepository prescriptionRepository;
	private final AppointmentRepository appointmentRepository;
	private final UserRepository userRepository;
	private final DoctorRepository doctorRepository;

	public PrescriptionService(
		PrescriptionRepository prescriptionRepository,
		AppointmentRepository appointmentRepository,
		UserRepository userRepository,
		DoctorRepository doctorRepository
	) {
		this.prescriptionRepository = prescriptionRepository;
		this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
		this.doctorRepository = doctorRepository;
	}

	@Transactional
	public PrescriptionResponse createMyPrescription(String doctorEmail, CreatePrescriptionRequest request) {
		Doctor doctor = findDoctorByEmail(doctorEmail);
		Appointment appointment = findAppointmentById(request.appointmentId());

		if (!appointment.getDoctor().getId().equals(doctor.getId())) {
			throw new IllegalArgumentException("You can only prescribe for your own appointments");
		}

		validateItemsJson(request.itemsJson());

		Prescription prescription = new Prescription();
		prescription.setAppointment(appointment);
		prescription.setPatient(appointment.getPatient());
		prescription.setDoctor(doctor);
		prescription.setDiagnosis(trimToNull(request.diagnosis()));
		prescription.setInstructions(trimToNull(request.instructions()));
		prescription.setItemsJson(request.itemsJson().trim());
		prescription.setValidUntil(request.validUntil());

		Prescription saved = prescriptionRepository.save(prescription);
		return toResponse(saved);
	}

	@Transactional
	public PrescriptionResponse updateMyPrescription(String doctorEmail, Long prescriptionId, UpdatePrescriptionRequest request) {
		Doctor doctor = findDoctorByEmail(doctorEmail);
		Prescription prescription = findPrescriptionById(prescriptionId);

		if (!prescription.getDoctor().getId().equals(doctor.getId())) {
			throw new IllegalArgumentException("You can only update your own prescriptions");
		}

		if (request.diagnosis() != null) {
			prescription.setDiagnosis(trimToNull(request.diagnosis()));
		}
		if (request.instructions() != null) {
			prescription.setInstructions(trimToNull(request.instructions()));
		}
		if (request.itemsJson() != null) {
			validateItemsJson(request.itemsJson());
			prescription.setItemsJson(request.itemsJson().trim());
		}
		if (request.validUntil() != null) {
			prescription.setValidUntil(request.validUntil());
		}

		Prescription saved = prescriptionRepository.save(prescription);
		return toResponse(saved);
	}

	public List<PrescriptionResponse> listMyPrescriptions(String doctorEmail) {
		Doctor doctor = findDoctorByEmail(doctorEmail);
		return prescriptionRepository.findByDoctorIdOrderByIssuedAtDesc(doctor.getId()).stream()
			.map(this::toResponse)
			.toList();
	}

	public PrescriptionResponse getMyPrescriptionById(String doctorEmail, Long prescriptionId) {
		Doctor doctor = findDoctorByEmail(doctorEmail);
		Prescription prescription = findPrescriptionById(prescriptionId);
		if (!prescription.getDoctor().getId().equals(doctor.getId())) {
			throw new IllegalArgumentException("You can only view your own prescriptions");
		}
		return toResponse(prescription);
	}

	public List<PrescriptionResponse> listPrescriptionsForAdmin(Long patientId, Long doctorId, Long appointmentId) {
		List<Prescription> result;
		if (appointmentId != null) {
			result = prescriptionRepository.findByAppointmentId(appointmentId);
		} else if (patientId != null) {
			result = prescriptionRepository.findByPatientIdOrderByIssuedAtDesc(patientId);
		} else if (doctorId != null) {
			result = prescriptionRepository.findByDoctorIdOrderByIssuedAtDesc(doctorId);
		} else {
			result = prescriptionRepository.findAll().stream()
				.sorted(Comparator.comparing(Prescription::getIssuedAt).reversed())
				.toList();
		}

		return result.stream().map(this::toResponse).toList();
	}

	public PrescriptionResponse getPrescriptionByIdForAdmin(Long prescriptionId) {
		return toResponse(findPrescriptionById(prescriptionId));
	}

	private Doctor findDoctorByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
		return doctorRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Doctor profile not found for current user"));
	}

	private Appointment findAppointmentById(Long appointmentId) {
		if (appointmentId == null) {
			throw new IllegalArgumentException("Appointment id is required");
		}
		return appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
	}

	private Prescription findPrescriptionById(Long prescriptionId) {
		return prescriptionRepository.findById(prescriptionId)
			.orElseThrow(() -> new IllegalArgumentException("Prescription not found: " + prescriptionId));
	}

	private void validateItemsJson(String itemsJson) {
		if (itemsJson == null || itemsJson.isBlank()) {
			throw new IllegalArgumentException("Prescription items are required");
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private PrescriptionResponse toResponse(Prescription prescription) {
		return new PrescriptionResponse(
			prescription.getId(),
			prescription.getAppointment().getId(),
			prescription.getPatient().getId(),
			prescription.getDoctor().getId(),
			prescription.getDiagnosis(),
			prescription.getInstructions(),
			prescription.getItemsJson(),
			prescription.getIssuedAt(),
			prescription.getValidUntil(),
			prescription.getCreatedAt()
		);
	}

	public record CreatePrescriptionRequest(
		Long appointmentId,
		String diagnosis,
		String instructions,
		String itemsJson,
		LocalDate validUntil
	) {
	}

	public record UpdatePrescriptionRequest(
		String diagnosis,
		String instructions,
		String itemsJson,
		LocalDate validUntil
	) {
	}

	public record PrescriptionResponse(
		Long id,
		Long appointmentId,
		Long patientId,
		Long doctorId,
		String diagnosis,
		String instructions,
		String itemsJson,
		OffsetDateTime issuedAt,
		LocalDate validUntil,
		OffsetDateTime createdAt
	) {
	}
}
