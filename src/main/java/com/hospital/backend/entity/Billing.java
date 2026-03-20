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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bills")
public class Billing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bill_number", nullable = false, unique = true, length = 50)
	private String billNumber;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false, unique = true)
	private Appointment appointment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Column(name = "consultation_fee", nullable = false, precision = 12, scale = 2)
	private BigDecimal consultationFee = BigDecimal.ZERO;

	@Column(name = "lab_fee", nullable = false, precision = 12, scale = 2)
	private BigDecimal labFee = BigDecimal.ZERO;

	@Column(name = "medication_fee", nullable = false, precision = 12, scale = 2)
	private BigDecimal medicationFee = BigDecimal.ZERO;

	@Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal taxAmount = BigDecimal.ZERO;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
	private BigDecimal amountPaid = BigDecimal.ZERO;

	@Column(name = "balance_due", nullable = false, precision = 12, scale = 2)
	private BigDecimal balanceDue = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BillingStatus status = BillingStatus.DRAFT;

	@Column(name = "issued_at")
	private OffsetDateTime issuedAt;

	@Column(name = "due_at")
	private OffsetDateTime dueAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

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

	public String getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(String billNumber) {
		this.billNumber = billNumber;
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

	public BigDecimal getConsultationFee() {
		return consultationFee;
	}

	public void setConsultationFee(BigDecimal consultationFee) {
		this.consultationFee = consultationFee;
	}

	public BigDecimal getLabFee() {
		return labFee;
	}

	public void setLabFee(BigDecimal labFee) {
		this.labFee = labFee;
	}

	public BigDecimal getMedicationFee() {
		return medicationFee;
	}

	public void setMedicationFee(BigDecimal medicationFee) {
		this.medicationFee = medicationFee;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public BigDecimal getBalanceDue() {
		return balanceDue;
	}

	public void setBalanceDue(BigDecimal balanceDue) {
		this.balanceDue = balanceDue;
	}

	public BillingStatus getStatus() {
		return status;
	}

	public void setStatus(BillingStatus status) {
		this.status = status;
	}

	public OffsetDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(OffsetDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public OffsetDateTime getDueAt() {
		return dueAt;
	}

	public void setDueAt(OffsetDateTime dueAt) {
		this.dueAt = dueAt;
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
}
