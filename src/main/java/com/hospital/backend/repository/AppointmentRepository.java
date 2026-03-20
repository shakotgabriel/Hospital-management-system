package com.hospital.backend.repository;

import com.hospital.backend.entity.Appointment;
import com.hospital.backend.entity.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByDoctorIdAndScheduledStartBetweenOrderByScheduledStartAsc(
		Long doctorId,
		OffsetDateTime dayStart,
		OffsetDateTime dayEnd
	);

	List<Appointment> findByPatientIdOrderByScheduledStartDesc(Long patientId);

	@Query(
		"""
		SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
		FROM Appointment a
		WHERE a.doctor.id = :doctorId
		  AND a.status IN :activeStatuses
		  AND a.scheduledStart < :newEnd
		  AND a.scheduledEnd > :newStart
		"""
	)
	boolean existsOverlappingAppointment(
		@Param("doctorId") Long doctorId,
		@Param("newStart") OffsetDateTime newStart,
		@Param("newEnd") OffsetDateTime newEnd,
		@Param("activeStatuses") Collection<AppointmentStatus> activeStatuses
	);
}
