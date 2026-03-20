package com.hospital.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePrescriptionRequest {

	@NotBlank(message = "Medication is required")
	@Size(min = 1, max = 255, message = "Medication must be between 1 and 255 characters")
	private String medication;

	@NotBlank(message = "Dosage is required")
	@Size(min = 1, max = 100, message = "Dosage must be between 1 and 100 characters")
	private String dosage;

	@NotBlank(message = "Frequency is required")
	@Size(min = 1, max = 100, message = "Frequency must be between 1 and 100 characters")
	private String frequency;

	@NotBlank(message = "Duration is required")
	@Size(min = 1, max = 100, message = "Duration must be between 1 and 100 characters")
	private String duration;

	private String notes;

	public CreatePrescriptionRequest() {
	}

	public String getMedication() {
		return medication;
	}

	public void setMedication(String medication) {
		this.medication = medication;
	}

	public String getDosage() {
		return dosage;
	}

	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
