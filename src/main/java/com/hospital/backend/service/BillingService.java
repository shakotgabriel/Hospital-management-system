package com.hospital.backend.service;

import com.hospital.backend.entity.Appointment;
import com.hospital.backend.entity.Billing;
import com.hospital.backend.entity.BillingStatus;
import com.hospital.backend.entity.Patient;
import com.hospital.backend.entity.User;
import com.hospital.backend.repository.AppointmentRepository;
import com.hospital.backend.repository.BillingRepository;
import com.hospital.backend.repository.PatientRepository;
import com.hospital.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

	private static final DateTimeFormatter BILL_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final BillingRepository billingRepository;
	private final AppointmentRepository appointmentRepository;
	private final UserRepository userRepository;
	private final PatientRepository patientRepository;

	public BillingService(
		BillingRepository billingRepository,
		AppointmentRepository appointmentRepository,
		UserRepository userRepository,
		PatientRepository patientRepository
	) {
		this.billingRepository = billingRepository;
		this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
		this.patientRepository = patientRepository;
	}

	@Transactional
	public BillingResponse createBillForAppointment(AdminCreateBillRequest request) {
		if (request.appointmentId() == null) {
			throw new IllegalArgumentException("Appointment id is required");
		}
		Appointment appointment = appointmentRepository.findById(request.appointmentId())
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + request.appointmentId()));

		billingRepository.findByAppointmentId(appointment.getId()).ifPresent(existing -> {
			throw new IllegalArgumentException("Bill already exists for appointment: " + appointment.getId());
		});

		Billing bill = new Billing();
		bill.setBillNumber(generateBillNumber());
		bill.setAppointment(appointment);
		bill.setPatient(appointment.getPatient());

		bill.setConsultationFee(normalizeMoney(request.consultationFee()));
		bill.setLabFee(normalizeMoney(request.labFee()));
		bill.setMedicationFee(normalizeMoney(request.medicationFee()));
		bill.setDiscountAmount(normalizeMoney(request.discountAmount()));
		bill.setTaxAmount(normalizeMoney(request.taxAmount()));
		bill.setAmountPaid(BigDecimal.ZERO);
		bill.setDueAt(request.dueAt());

		recalculateAmounts(bill);
		if (Boolean.TRUE.equals(request.issueNow())) {
			bill.setStatus(bill.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 ? BillingStatus.PAID : BillingStatus.ISSUED);
			bill.setIssuedAt(OffsetDateTime.now());
		} else {
			bill.setStatus(BillingStatus.DRAFT);
		}

		return toResponse(billingRepository.save(bill));
	}

	@Transactional
	public BillingResponse updateBillForAdmin(Long billingId, AdminUpdateBillRequest request) {
		Billing bill = findBill(billingId);
		if (bill.getStatus() == BillingStatus.VOID) {
			throw new IllegalArgumentException("Void bill cannot be updated");
		}

		if (request.consultationFee() != null) {
			bill.setConsultationFee(normalizeMoney(request.consultationFee()));
		}
		if (request.labFee() != null) {
			bill.setLabFee(normalizeMoney(request.labFee()));
		}
		if (request.medicationFee() != null) {
			bill.setMedicationFee(normalizeMoney(request.medicationFee()));
		}
		if (request.discountAmount() != null) {
			bill.setDiscountAmount(normalizeMoney(request.discountAmount()));
		}
		if (request.taxAmount() != null) {
			bill.setTaxAmount(normalizeMoney(request.taxAmount()));
		}
		if (request.dueAt() != null) {
			bill.setDueAt(request.dueAt());
		}

		recalculateAmounts(bill);
		if (bill.getStatus() != BillingStatus.DRAFT && bill.getStatus() != BillingStatus.VOID) {
			bill.setStatus(computePaymentStatus(bill));
		}

		return toResponse(billingRepository.save(bill));
	}

	@Transactional
	public BillingResponse issueBillForAdmin(Long billingId) {
		Billing bill = findBill(billingId);
		if (bill.getStatus() == BillingStatus.VOID) {
			throw new IllegalArgumentException("Void bill cannot be issued");
		}
		bill.setIssuedAt(OffsetDateTime.now());
		bill.setStatus(computePaymentStatus(bill) == BillingStatus.PAID ? BillingStatus.PAID : BillingStatus.ISSUED);
		return toResponse(billingRepository.save(bill));
	}

	@Transactional
	public BillingResponse voidBillForAdmin(Long billingId) {
		Billing bill = findBill(billingId);
		bill.setStatus(BillingStatus.VOID);
		return toResponse(billingRepository.save(bill));
	}

	@Transactional
	public BillingResponse recordMyPayment(String userEmail, Long billingId, RecordPaymentRequest request) {
		Billing bill = findBill(billingId);
		if (bill.getStatus() == BillingStatus.VOID) {
			throw new IllegalArgumentException("Void bill cannot be paid");
		}

		Patient patient = findPatientByEmail(userEmail);
		if (!bill.getPatient().getId().equals(patient.getId())) {
			throw new IllegalArgumentException("You can only pay your own bill");
		}

		BigDecimal paymentAmount = normalizeMoney(request.amount());
		if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Payment amount must be greater than zero");
		}

		BigDecimal newAmountPaid = bill.getAmountPaid().add(paymentAmount);
		if (newAmountPaid.compareTo(bill.getTotalAmount()) > 0) {
			throw new IllegalArgumentException("Payment exceeds total amount");
		}

		bill.setAmountPaid(newAmountPaid);
		recalculateAmounts(bill);
		bill.setStatus(computePaymentStatus(bill));

		if (bill.getIssuedAt() == null) {
			bill.setIssuedAt(OffsetDateTime.now());
		}

		return toResponse(billingRepository.save(bill));
	}

	public List<BillingResponse> listMyBills(String userEmail) {
		Patient patient = findPatientByEmail(userEmail);
		return billingRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
			.map(this::toResponse)
			.toList();
	}

	public BillingResponse getMyBill(String userEmail, Long billingId) {
		Patient patient = findPatientByEmail(userEmail);
		Billing bill = findBill(billingId);
		if (!bill.getPatient().getId().equals(patient.getId())) {
			throw new IllegalArgumentException("You can only view your own bill");
		}
		return toResponse(bill);
	}

	public List<BillingResponse> listBillsForAdmin() {
		return billingRepository.findAll().stream()
			.sorted(Comparator.comparing(Billing::getCreatedAt).reversed())
			.map(this::toResponse)
			.toList();
	}

	public BillingResponse getBillForAdmin(Long billingId) {
		return toResponse(findBill(billingId));
	}

	private Billing findBill(Long billingId) {
		return billingRepository.findById(billingId)
			.orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billingId));
	}

	private Patient findPatientByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
		return patientRepository.findByUserId(user.getId())
			.orElseThrow(() -> new IllegalArgumentException("Patient profile not found for current user"));
	}

	private void recalculateAmounts(Billing bill) {
		BigDecimal total = bill.getConsultationFee()
			.add(bill.getLabFee())
			.add(bill.getMedicationFee())
			.add(bill.getTaxAmount())
			.subtract(bill.getDiscountAmount());

		if (total.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Total amount cannot be negative");
		}

		bill.setTotalAmount(total);
		BigDecimal balance = total.subtract(bill.getAmountPaid());
		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Amount paid cannot exceed total amount");
		}
		bill.setBalanceDue(balance);
	}

	private BillingStatus computePaymentStatus(Billing bill) {
		if (bill.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
			return BillingStatus.PAID;
		}
		if (bill.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
			return BillingStatus.PARTIAL;
		}
		return BillingStatus.ISSUED;
	}

	private BigDecimal normalizeMoney(BigDecimal amount) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Amounts cannot be negative");
		}
		return amount;
	}

	private String generateBillNumber() {
		for (int i = 0; i < 10; i++) {
			String candidate = "BILL-" + LocalDate.now().format(BILL_DATE) + "-"
				+ UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
			if (billingRepository.findByBillNumber(candidate).isEmpty()) {
				return candidate;
			}
		}
		throw new IllegalStateException("Failed to generate unique bill number");
	}

	private BillingResponse toResponse(Billing bill) {
		return new BillingResponse(
			bill.getId(),
			bill.getBillNumber(),
			bill.getAppointment().getId(),
			bill.getPatient().getId(),
			bill.getConsultationFee(),
			bill.getLabFee(),
			bill.getMedicationFee(),
			bill.getDiscountAmount(),
			bill.getTaxAmount(),
			bill.getTotalAmount(),
			bill.getAmountPaid(),
			bill.getBalanceDue(),
			bill.getStatus(),
			bill.getIssuedAt(),
			bill.getDueAt(),
			bill.getCreatedAt(),
			bill.getUpdatedAt()
		);
	}

	public record AdminCreateBillRequest(
		Long appointmentId,
		BigDecimal consultationFee,
		BigDecimal labFee,
		BigDecimal medicationFee,
		BigDecimal discountAmount,
		BigDecimal taxAmount,
		OffsetDateTime dueAt,
		Boolean issueNow
	) {
	}

	public record AdminUpdateBillRequest(
		BigDecimal consultationFee,
		BigDecimal labFee,
		BigDecimal medicationFee,
		BigDecimal discountAmount,
		BigDecimal taxAmount,
		OffsetDateTime dueAt
	) {
	}

	public record RecordPaymentRequest(BigDecimal amount) {
	}

	public record BillingResponse(
		Long id,
		String billNumber,
		Long appointmentId,
		Long patientId,
		BigDecimal consultationFee,
		BigDecimal labFee,
		BigDecimal medicationFee,
		BigDecimal discountAmount,
		BigDecimal taxAmount,
		BigDecimal totalAmount,
		BigDecimal amountPaid,
		BigDecimal balanceDue,
		BillingStatus status,
		OffsetDateTime issuedAt,
		OffsetDateTime dueAt,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
	}
}
