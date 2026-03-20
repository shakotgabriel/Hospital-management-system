package com.hospital.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointments")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@Column(name = "scheduled_start", nullable = false)
	private OffsetDateTime scheduledStart;

	@Column(name = "scheduled_end", nullable = false)
	private OffsetDateTime scheduledEnd;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AppointmentStatus status = AppointmentStatus.BOOKED;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@OneToOne(mappedBy = "appointment")
	private Billing bill;

	@OneToMany(mappedBy = "appointment")
	private List<Prescription> prescriptions = new ArrayList<>();

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Billing getBill() {
		return bill;
	}

	public void setBill(Billing bill) {
		this.bill = bill;
	}

	public List<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescriptions(List<Prescription> prescriptions) {
		this.prescriptions = prescriptions;
	}
}
