package com.hospital.backend.repository;

import com.hospital.backend.entity.Billing;
import com.hospital.backend.entity.BillingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillingRepository extends JpaRepository<Billing, Long> {

	interface BillingStatusCountProjection {
		BillingStatus getStatus();

		Long getCount();
	}

	Optional<Billing> findByBillNumber(String billNumber);

	Optional<Billing> findByAppointmentId(Long appointmentId);

	List<Billing> findByPatientIdOrderByCreatedAtDesc(Long patientId);

	List<Billing> findByCreatedAtBetweenOrderByCreatedAtAsc(OffsetDateTime fromInclusive, OffsetDateTime toInclusive);

	@Query("SELECT b.status as status, COUNT(b) as count FROM Billing b GROUP BY b.status")
	List<BillingStatusCountProjection> countByStatus();

	@Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.createdAt BETWEEN :fromInclusive AND :toInclusive")
	BigDecimal sumTotalAmountCreatedBetween(
		@Param("fromInclusive") OffsetDateTime fromInclusive,
		@Param("toInclusive") OffsetDateTime toInclusive
	);

	@Query("SELECT COALESCE(SUM(b.amountPaid), 0) FROM Billing b WHERE b.createdAt BETWEEN :fromInclusive AND :toInclusive")
	BigDecimal sumAmountPaidCreatedBetween(
		@Param("fromInclusive") OffsetDateTime fromInclusive,
		@Param("toInclusive") OffsetDateTime toInclusive
	);

	@Query("SELECT COALESCE(SUM(b.balanceDue), 0) FROM Billing b WHERE b.status <> com.hospital.backend.entity.BillingStatus.VOID")
	BigDecimal sumOutstandingBalance();
}
