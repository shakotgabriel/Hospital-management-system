package com.hospital.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private Appointment appointment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@Column(columnDefinition = "TEXT")
	private String diagnosis;

	@Column(columnDefinition = "TEXT")
	private String instructions;

	@Column(name = "items_json", nullable = false, columnDefinition = "TEXT")
	private String itemsJson;

	@Column(name = "issued_at", nullable = false)
	private OffsetDateTime issuedAt;

	@Column(name = "valid_until")
	private LocalDate validUntil;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		if (issuedAt == null) {
			issuedAt = now;
		}
		createdAt = now;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getItemsJson() {
		return itemsJson;
	}

	public void setItemsJson(String itemsJson) {
		this.itemsJson = itemsJson;
	}

	public OffsetDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(OffsetDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDate validUntil) {
		this.validUntil = validUntil;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
