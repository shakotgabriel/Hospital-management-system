package com.hospital.backend.repository;

import com.hospital.backend.entity.Prescription;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

	List<Prescription> findByPatientIdOrderByIssuedAtDesc(Long patientId);

	List<Prescription> findByDoctorIdOrderByIssuedAtDesc(Long doctorId);

	List<Prescription> findByAppointmentId(Long appointmentId);

	long countByIssuedAtBetween(OffsetDateTime fromInclusive, OffsetDateTime toInclusive);
}
