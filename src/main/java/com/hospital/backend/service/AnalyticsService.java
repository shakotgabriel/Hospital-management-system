package com.hospital.backend.service;

import com.hospital.backend.entity.AppointmentStatus;
import com.hospital.backend.entity.Billing;
import com.hospital.backend.entity.BillingStatus;
import com.hospital.backend.repository.AppointmentRepository;
import com.hospital.backend.repository.BillingRepository;
import com.hospital.backend.repository.DoctorRepository;
import com.hospital.backend.repository.PatientRepository;
import com.hospital.backend.repository.PrescriptionRepository;
import com.hospital.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

	private final UserRepository userRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final AppointmentRepository appointmentRepository;
	private final PrescriptionRepository prescriptionRepository;
	private final BillingRepository billingRepository;

	public AnalyticsService(
		UserRepository userRepository,
		PatientRepository patientRepository,
		DoctorRepository doctorRepository,
		AppointmentRepository appointmentRepository,
		PrescriptionRepository prescriptionRepository,
		BillingRepository billingRepository
	) {
		this.userRepository = userRepository;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
		this.appointmentRepository = appointmentRepository;
		this.prescriptionRepository = prescriptionRepository;
		this.billingRepository = billingRepository;
	}

	public OverviewResponse getOverview(int days, int topDoctorsLimit) {
		if (days < 1 || days > 365) {
			throw new IllegalArgumentException("Days must be between 1 and 365");
		}
		if (topDoctorsLimit < 1 || topDoctorsLimit > 20) {
			throw new IllegalArgumentException("Top doctors limit must be between 1 and 20");
		}

		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime fromInclusive = startOfDay(now.minusDays(days - 1));
		OffsetDateTime toInclusive = now;

		KpiTotals totals = new KpiTotals(
			userRepository.count(),
			patientRepository.count(),
			doctorRepository.count(),
			appointmentRepository.count(),
			appointmentRepository.countByScheduledStartBetween(fromInclusive, toInclusive),
			prescriptionRepository.count(),
			prescriptionRepository.countByIssuedAtBetween(fromInclusive, toInclusive),
			billingRepository.count()
		);

		FinancialSummary financials = new FinancialSummary(
			normalizeMoney(billingRepository.sumTotalAmountCreatedBetween(fromInclusive, toInclusive)),
			normalizeMoney(billingRepository.sumAmountPaidCreatedBetween(fromInclusive, toInclusive)),
			normalizeMoney(billingRepository.sumOutstandingBalance())
		);

		List<StatusCount> appointmentStatusBreakdown = buildAppointmentStatusBreakdown();
		List<StatusCount> billingStatusBreakdown = buildBillingStatusBreakdown();
		List<DailyAmountPoint> dailyBilledTrend = buildDailyBilledTrend(fromInclusive, toInclusive, days);

		List<DoctorLoad> topDoctorsByAppointments = appointmentRepository
			.findTopDoctorsByAppointmentCountBetween(
				fromInclusive,
				toInclusive,
				PageRequest.of(0, topDoctorsLimit)
			)
			.stream()
			.map(row -> new DoctorLoad(row.getDoctorId(), row.getDoctorName(), safeLong(row.getAppointmentCount())))
			.toList();

		return new OverviewResponse(
			now,
			days,
			totals,
			financials,
			appointmentStatusBreakdown,
			billingStatusBreakdown,
			dailyBilledTrend,
			topDoctorsByAppointments
		);
	}

	private List<StatusCount> buildAppointmentStatusBreakdown() {
		Map<AppointmentStatus, Long> countMap = new LinkedHashMap<>();
		Arrays.stream(AppointmentStatus.values()).forEach(status -> countMap.put(status, 0L));

		appointmentRepository.countByStatus().forEach(row -> {
			if (row.getStatus() != null) {
				countMap.put(row.getStatus(), safeLong(row.getCount()));
			}
		});

		return countMap.entrySet().stream()
			.map(entry -> new StatusCount(entry.getKey().name(), entry.getValue()))
			.toList();
	}

	private List<StatusCount> buildBillingStatusBreakdown() {
		Map<BillingStatus, Long> countMap = new LinkedHashMap<>();
		Arrays.stream(BillingStatus.values()).forEach(status -> countMap.put(status, 0L));

		billingRepository.countByStatus().forEach(row -> {
			if (row.getStatus() != null) {
				countMap.put(row.getStatus(), safeLong(row.getCount()));
			}
		});

		return countMap.entrySet().stream()
			.map(entry -> new StatusCount(entry.getKey().name(), entry.getValue()))
			.toList();
	}

	private List<DailyAmountPoint> buildDailyBilledTrend(OffsetDateTime fromInclusive, OffsetDateTime toInclusive, int days) {
		LocalDate startDate = fromInclusive.toLocalDate();
		Map<LocalDate, BigDecimal> byDay = new LinkedHashMap<>();
		for (int i = 0; i < days; i++) {
			byDay.put(startDate.plusDays(i), BigDecimal.ZERO);
		}

		List<Billing> bills = billingRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(fromInclusive, toInclusive);
		for (Billing bill : bills) {
			LocalDate date = bill.getCreatedAt() != null ? bill.getCreatedAt().toLocalDate() : null;
			if (date != null && byDay.containsKey(date) && bill.getStatus() != BillingStatus.VOID) {
				byDay.put(date, byDay.get(date).add(normalizeMoney(bill.getTotalAmount())));
			}
		}

		return byDay.entrySet().stream()
			.map(entry -> new DailyAmountPoint(entry.getKey(), entry.getValue()))
			.toList();
	}

	private OffsetDateTime startOfDay(OffsetDateTime value) {
		ZoneOffset offset = value.getOffset();
		return value.toLocalDate().atStartOfDay().atOffset(offset);
	}

	private BigDecimal normalizeMoney(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private long safeLong(Long value) {
		return value == null ? 0L : value;
	}

	public record OverviewResponse(
		OffsetDateTime generatedAt,
		int periodDays,
		KpiTotals totals,
		FinancialSummary financials,
		List<StatusCount> appointmentStatusBreakdown,
		List<StatusCount> billingStatusBreakdown,
		List<DailyAmountPoint> dailyBilledTrend,
		List<DoctorLoad> topDoctorsByAppointments
	) {
	}

	public record KpiTotals(
		long users,
		long patients,
		long doctors,
		long appointments,
		long appointmentsInPeriod,
		long prescriptions,
		long prescriptionsInPeriod,
		long bills
	) {
	}

	public record FinancialSummary(
		BigDecimal totalBilledInPeriod,
		BigDecimal totalCollectedInPeriod,
		BigDecimal totalOutstanding
	) {
	}

	public record StatusCount(String status, long count) {
	}

	public record DailyAmountPoint(LocalDate date, BigDecimal amount) {
	}

	public record DoctorLoad(Long doctorId, String doctorName, long appointmentCount) {
	}
}
