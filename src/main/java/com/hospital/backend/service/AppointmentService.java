package com.hospital.backend.service;

import com.hospital.backend.entity.Appointment;
import com.hospital.backend.entity.AppointmentStatus;
import com.hospital.backend.entity.Doctor;
import com.hospital.backend.entity.Patient;
import com.hospital.backend.entity.User;
import com.hospital.backend.repository.AppointmentRepository;
import com.hospital.backend.repository.DoctorRepository;
import com.hospital.backend.repository.PatientRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

	private static final EnumSet<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
		AppointmentStatus.BOOKED,
		AppointmentStatus.CONFIRMED,
		AppointmentStatus.RESCHEDULED
	);

	private final AppointmentRepository appointmentRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final UserRepository userRepository;

	public AppointmentService(
		AppointmentRepository appointmentRepository,
		PatientRepository patientRepository,
		DoctorRepository doctorRepository,
		UserRepository userRepository
	) {
		this.appointmentRepository = appointmentRepository;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public AppointmentResponse bookMyAppointment(String userEmail, BookAppointmentRequest request) {
		Patient patient = findPatientByEmail(userEmail);
		Doctor doctor = findDoctorById(request.doctorId());
		validateTimeWindow(request.scheduledStart(), request.scheduledEnd());

		if (appointmentRepository.existsOverlappingAppointment(
			doctor.getId(),
			request.scheduledStart(),
			request.scheduledEnd(),
			ACTIVE_STATUSES
		)) {
			throw new IllegalArgumentException("Doctor already has an overlapping appointment");
		}

		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setScheduledStart(request.scheduledStart());
		appointment.setScheduledEnd(request.scheduledEnd());
		appointment.setReason(trimToNull(request.reason()));
		appointment.setStatus(AppointmentStatus.BOOKED);

		User actor = findUserByEmail(userEmail);
		appointment.setCreatedBy(actor);
		appointment.setUpdatedBy(actor);

		Appointment saved = appointmentRepository.save(appointment);
		return toResponse(saved);
	}

	public List<AppointmentResponse> listMyAppointments(String userEmail) {
		User user = findUserByEmail(userEmail);
		return patientRepository.findByUserId(user.getId())
			.map(patient -> appointmentRepository.findByPatientIdOrderByScheduledStartDesc(patient.getId()).stream()
				.map(this::toResponse)
				.toList())
			.orElseGet(() -> doctorRepository.findByUserId(user.getId())
				.map(doctor -> appointmentRepository.findByDoctorIdOrderByScheduledStartDesc(doctor.getId()).stream()
					.map(this::toResponse)
					.toList())
				.orElseThrow(() -> new IllegalArgumentException("No patient or doctor profile found for current user")));
	}

	@Transactional
	public AppointmentResponse rescheduleMyAppointment(String userEmail, Long appointmentId, RescheduleAppointmentRequest request) {
		Appointment appointment = findAppointment(appointmentId);
		User actor = findUserByEmail(userEmail);
		enforceParticipantAccess(actor, appointment);

		validateTimeWindow(request.scheduledStart(), request.scheduledEnd());
		if (appointmentRepository.existsOverlappingAppointmentExcluding(
			appointment.getDoctor().getId(),
			appointment.getId(),
			request.scheduledStart(),
			request.scheduledEnd(),
			ACTIVE_STATUSES
		)) {
			throw new IllegalArgumentException("Doctor already has an overlapping appointment");
		}

		if (appointment.getStatus() == AppointmentStatus.CANCELED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalArgumentException("Completed or canceled appointment cannot be rescheduled");
		}

		appointment.setScheduledStart(request.scheduledStart());
		appointment.setScheduledEnd(request.scheduledEnd());
		if (request.reason() != null) {
			appointment.setReason(trimToNull(request.reason()));
		}
		if (request.notes() != null) {
			appointment.setNotes(trimToNull(request.notes()));
		}
		appointment.setStatus(AppointmentStatus.RESCHEDULED);
		appointment.setUpdatedBy(actor);

		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentResponse cancelMyAppointment(String userEmail, Long appointmentId, String notes) {
		Appointment appointment = findAppointment(appointmentId);
		User actor = findUserByEmail(userEmail);
		enforceParticipantAccess(actor, appointment);

		if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalArgumentException("Completed appointment cannot be canceled");
		}

		appointment.setStatus(AppointmentStatus.CANCELED);
		if (notes != null) {
			appointment.setNotes(trimToNull(notes));
		}
		appointment.setUpdatedBy(actor);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentResponse confirmAppointmentAsDoctor(String doctorEmail, Long appointmentId, String notes) {
		Appointment appointment = findAppointment(appointmentId);
		Doctor doctor = findDoctorByEmail(doctorEmail);
		if (!appointment.getDoctor().getId().equals(doctor.getId())) {
			throw new IllegalArgumentException("You can only confirm your own appointments");
		}
		if (appointment.getStatus() == AppointmentStatus.CANCELED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalArgumentException("Appointment cannot be confirmed from current status");
		}

		appointment.setStatus(AppointmentStatus.CONFIRMED);
		if (notes != null) {
			appointment.setNotes(trimToNull(notes));
		}
		appointment.setUpdatedBy(findUserByEmail(doctorEmail));
		return toResponse(appointmentRepository.save(appointment));
	}

	@Transactional
	public AppointmentResponse completeAppointmentAsDoctor(String doctorEmail, Long appointmentId, String notes) {
		Appointment appointment = findAppointment(appointmentId);
		Doctor doctor = findDoctorByEmail(doctorEmail);
		if (!appointment.getDoctor().getId().equals(doctor.getId())) {
			throw new IllegalArgumentException("You can only complete your own appointments");
		}
		if (appointment.getStatus() == AppointmentStatus.CANCELED) {
			throw new IllegalArgumentException("Canceled appointment cannot be completed");
		}

		appointment.setStatus(AppointmentStatus.COMPLETED);
		if (notes != null) {
			appointment.setNotes(trimToNull(notes));
		}
		appointment.setUpdatedBy(findUserByEmail(doctorEmail));
		return toResponse(appointmentRepository.save(appointment));
	}

	public List<AppointmentResponse> listAppointmentsForAdmin() {
		return appointmentRepository.findAll().stream()
			.sorted(Comparator.comparing(Appointment::getScheduledStart).reversed())
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public AppointmentResponse bookAppointmentForAdmin(String actorEmail, AdminBookAppointmentRequest request) {
		Patient patient = findPatientById(request.patientId());
		Doctor doctor = findDoctorById(request.doctorId());
		validateTimeWindow(request.scheduledStart(), request.scheduledEnd());

		if (appointmentRepository.existsOverlappingAppointment(
			doctor.getId(),
			request.scheduledStart(),
			request.scheduledEnd(),
			ACTIVE_STATUSES
		)) {
			throw new IllegalArgumentException("Doctor already has an overlapping appointment");
		}

		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setScheduledStart(request.scheduledStart());
		appointment.setScheduledEnd(request.scheduledEnd());
		appointment.setReason(trimToNull(request.reason()));
		appointment.setNotes(trimToNull(request.notes()));
		appointment.setStatus(AppointmentStatus.BOOKED);

		User actor = findUserByEmail(actorEmail);
		appointment.setCreatedBy(actor);
		appointment.setUpdatedBy(actor);

		Appointment saved = appointmentRepository.save(appointment);
		return toResponse(saved);
	}

	public AppointmentResponse getAppointmentByIdForAdmin(Long appointmentId) {
		return toResponse(findAppointment(appointmentId));
	}

	@Transactional
	public AppointmentResponse updateAppointmentForAdmin(Long appointmentId, AdminUpdateAppointmentRequest request) {
		Appointment appointment = findAppointment(appointmentId);

		if (request.patientId() != null) {
			Patient patient = patientRepository.findById(request.patientId())
				.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + request.patientId()));
			appointment.setPatient(patient);
		}
		if (request.doctorId() != null) {
			Doctor doctor = findDoctorById(request.doctorId());
			appointment.setDoctor(doctor);
		}
		if (request.scheduledStart() != null && request.scheduledEnd() != null) {
			validateTimeWindow(request.scheduledStart(), request.scheduledEnd());
			if (appointmentRepository.existsOverlappingAppointmentExcluding(
				appointment.getDoctor().getId(),
				appointment.getId(),
				request.scheduledStart(),
				request.scheduledEnd(),
				ACTIVE_STATUSES
			)) {
				throw new IllegalArgumentException("Doctor already has an overlapping appointment");
			}
			appointment.setScheduledStart(request.scheduledStart());
			appointment.setScheduledEnd(request.scheduledEnd());
		}
		if (request.status() != null) {
			appointment.setStatus(request.status());
		}
		if (request.reason() != null) {
			appointment.setReason(trimToNull(request.reason()));
		}
		if (request.notes() != null) {
			appointment.setNotes(trimToNull(request.notes()));
		}

		return toResponse(appointmentRepository.save(appointment));
	}

	private Appointment findAppointment(Long appointmentId) {
		return appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
	}

	private Doctor findDoctorById(Long doctorId) {
		if (doctorId == null) {
			throw new IllegalArgumentException("Doctor id is required");
		}
		return doctorRepository.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
	}

	private Patient findPatientById(Long patientId) {
		if (patientId == null) {
			throw new IllegalArgumentException("Patient id is required");
		}
		return patientRepository.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
	}

	private User findUserByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
	}

	private Patient findPatientByEmail(String email) {
		User user = findUserByEmail(email);
		return patientRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Patient profile not found for current user"));
	}

	private Doctor findDoctorByEmail(String email) {
		User user = findUserByEmail(email);
		return doctorRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Doctor profile not found for current user"));
	}

	private void enforceParticipantAccess(User actor, Appointment appointment) {
		Long actorUserId = actor.getId();
		Long patientUserId = appointment.getPatient().getUser() != null ? appointment.getPatient().getUser().getId() : null;
		Long doctorUserId = appointment.getDoctor().getUser() != null ? appointment.getDoctor().getUser().getId() : null;
		if (!actorUserId.equals(patientUserId) && !actorUserId.equals(doctorUserId)) {
			throw new IllegalArgumentException("You can only update your own appointments");
		}
	}

	private void validateTimeWindow(OffsetDateTime start, OffsetDateTime end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Scheduled start and end are required");
		}
		if (!end.isAfter(start)) {
			throw new IllegalArgumentException("Scheduled end must be after scheduled start");
		}
		if (!start.isAfter(OffsetDateTime.now().minusMinutes(1))) {
			throw new IllegalArgumentException("Appointment must be scheduled in the future");
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private AppointmentResponse toResponse(Appointment appointment) {
		return new AppointmentResponse(
			appointment.getId(),
			appointment.getPatient().getId(),
			appointment.getDoctor().getId(),
			appointment.getScheduledStart(),
			appointment.getScheduledEnd(),
			appointment.getStatus(),
			appointment.getReason(),
			appointment.getNotes(),
			appointment.getCreatedAt(),
			appointment.getUpdatedAt()
		);
	}

	public record BookAppointmentRequest(
		Long doctorId,
		OffsetDateTime scheduledStart,
		OffsetDateTime scheduledEnd,
		String reason
	) {
	}

	public record RescheduleAppointmentRequest(
		OffsetDateTime scheduledStart,
		OffsetDateTime scheduledEnd,
		String reason,
		String notes
	) {
	}

	public record AdminBookAppointmentRequest(
		Long patientId,
		Long doctorId,
		OffsetDateTime scheduledStart,
		OffsetDateTime scheduledEnd,
		String reason,
		String notes
	) {
	}

	public record AdminUpdateAppointmentRequest(
		Long patientId,
		Long doctorId,
		OffsetDateTime scheduledStart,
		OffsetDateTime scheduledEnd,
		AppointmentStatus status,
		String reason,
		String notes
	) {
	}

	public record AppointmentResponse(
		Long id,
		Long patientId,
		Long doctorId,
		OffsetDateTime scheduledStart,
		OffsetDateTime scheduledEnd,
		AppointmentStatus status,
		String reason,
		String notes,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
	}
}
