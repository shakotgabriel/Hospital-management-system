package com.hospital.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;

public class BookAppointmentRequest {

	@NotNull(message = "Doctor ID is required")
	@Positive(message = "Doctor ID must be a positive number")
	private Long doctorId;

	@NotNull(message = "Appointment start time is required")
	@Future(message = "Appointment start time must be in the future")
	private OffsetDateTime scheduledStart;

	@NotNull(message = "Appointment end time is required")
	@Future(message = "Appointment end time must be in the future")
	private OffsetDateTime scheduledEnd;

	private String notes;

	public BookAppointmentRequest() {
	}

	public BookAppointmentRequest(Long doctorId, OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd) {
		this.doctorId = doctorId;
		this.scheduledStart = scheduledStart;
		this.scheduledEnd = scheduledEnd;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public OffsetDateTime getScheduledStart() {
		return scheduledStart;
	}

	public void setScheduledStart(OffsetDateTime scheduledStart) {
		this.scheduledStart = scheduledStart;
	}

	public OffsetDateTime getScheduledEnd() {
		return scheduledEnd;
	}

	public void setScheduledEnd(OffsetDateTime scheduledEnd) {
		this.scheduledEnd = scheduledEnd;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
