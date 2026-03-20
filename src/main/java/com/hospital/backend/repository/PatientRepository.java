package com.hospital.backend.repository;

import com.hospital.backend.entity.Patient;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

	Optional<Patient> findByPatientNumber(String patientNumber);

	Optional<Patient> findByUserId(Long userId);

	Page<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
		String firstName,
		String lastName,
		Pageable pageable
	);
}
