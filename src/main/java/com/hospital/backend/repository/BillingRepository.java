package com.hospital.backend.repository;

import com.hospital.backend.entity.Billing;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Long> {

	Optional<Billing> findByBillNumber(String billNumber);

	Optional<Billing> findByAppointmentId(Long appointmentId);

	List<Billing> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
