package com.hospital.backend.repository;

import com.hospital.backend.entity.Doctor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);
}
