package com.hospital.backend.repository;

import com.hospital.backend.entity.Appointment;
import com.hospital.backend.entity.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	interface AppointmentStatusCountProjection {
		AppointmentStatus getStatus();

		Long getCount();
	}

	interface DoctorAppointmentCountProjection {
		Long getDoctorId();

		String getDoctorName();

		Long getAppointmentCount();
	}

	List<Appointment> findByDoctorIdAndScheduledStartBetweenOrderByScheduledStartAsc(
		Long doctorId,
		OffsetDateTime dayStart,
		OffsetDateTime dayEnd
	);

	List<Appointment> findByDoctorIdOrderByScheduledStartDesc(Long doctorId);

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

	@Query(
		"""
		SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
		FROM Appointment a
		WHERE a.doctor.id = :doctorId
		  AND a.id <> :appointmentId
		  AND a.status IN :activeStatuses
		  AND a.scheduledStart < :newEnd
		  AND a.scheduledEnd > :newStart
		"""
	)
	boolean existsOverlappingAppointmentExcluding(
		@Param("doctorId") Long doctorId,
		@Param("appointmentId") Long appointmentId,
		@Param("newStart") OffsetDateTime newStart,
		@Param("newEnd") OffsetDateTime newEnd,
		@Param("activeStatuses") Collection<AppointmentStatus> activeStatuses
	);

	@Query("SELECT a.status as status, COUNT(a) as count FROM Appointment a GROUP BY a.status")
	List<AppointmentStatusCountProjection> countByStatus();

	long countByScheduledStartBetween(OffsetDateTime fromInclusive, OffsetDateTime toInclusive);

	@Query(
		"""
		SELECT d.id as doctorId,
		       CONCAT(u.firstName, ' ', u.lastName) as doctorName,
		       COUNT(a) as appointmentCount
		FROM Appointment a
		JOIN a.doctor d
		JOIN d.user u
		WHERE a.scheduledStart BETWEEN :fromInclusive AND :toInclusive
		GROUP BY d.id, u.firstName, u.lastName
		ORDER BY COUNT(a) DESC
		"""
	)
	List<DoctorAppointmentCountProjection> findTopDoctorsByAppointmentCountBetween(
		@Param("fromInclusive") OffsetDateTime fromInclusive,
		@Param("toInclusive") OffsetDateTime toInclusive,
		Pageable pageable
	);
}
