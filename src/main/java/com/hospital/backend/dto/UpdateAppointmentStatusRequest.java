package com.hospital.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateAppointmentStatusRequest {

	@NotBlank(message = "Status is required")
	@Size(min = 1, max = 50, message = "Status must be between 1 and 50 characters")
	private String status;

	private String notes;

	public UpdateAppointmentStatusRequest() {
	}

	public UpdateAppointmentStatusRequest(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
