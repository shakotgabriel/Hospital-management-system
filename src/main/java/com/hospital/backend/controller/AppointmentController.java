package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.AppointmentService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping("/appointments")
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> bookMyAppointment(
		Principal principal,
		@RequestBody AppointmentService.BookAppointmentRequest request
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.bookMyAppointment(principal.getName(), request);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Appointment booked", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/appointments/me")
	public ResponseEntity<ApiResponse<List<AppointmentService.AppointmentResponse>>> listMyAppointments(Principal principal) {
		try {
			List<AppointmentService.AppointmentResponse> response = appointmentService.listMyAppointments(principal.getName());
			return ResponseEntity.ok(ApiResponse.ok("Appointments fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/appointments/{appointmentId}/reschedule")
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> rescheduleAppointment(
		Principal principal,
		@PathVariable Long appointmentId,
		@RequestBody AppointmentService.RescheduleAppointmentRequest request
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.rescheduleMyAppointment(
				principal.getName(),
				appointmentId,
				request
			);
			return ResponseEntity.ok(ApiResponse.ok("Appointment rescheduled", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/appointments/{appointmentId}/cancel")
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> cancelAppointment(
		Principal principal,
		@PathVariable Long appointmentId,
		@RequestParam(required = false) String notes
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.cancelMyAppointment(
				principal.getName(),
				appointmentId,
				notes
			);
			return ResponseEntity.ok(ApiResponse.ok("Appointment canceled", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/doctors/appointments/{appointmentId}/confirm")
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> confirmAsDoctor(
		Principal principal,
		@PathVariable Long appointmentId,
		@RequestParam(required = false) String notes
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.confirmAppointmentAsDoctor(
				principal.getName(),
				appointmentId,
				notes
			);
			return ResponseEntity.ok(ApiResponse.ok("Appointment confirmed", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/doctors/appointments/{appointmentId}/complete")
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> completeAsDoctor(
		Principal principal,
		@PathVariable Long appointmentId,
		@RequestParam(required = false) String notes
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.completeAppointmentAsDoctor(
				principal.getName(),
				appointmentId,
				notes
			);
			return ResponseEntity.ok(ApiResponse.ok("Appointment completed", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping({ "/admin/appointments", "/reception/appointments" })
	public ResponseEntity<ApiResponse<List<AppointmentService.AppointmentResponse>>> listAppointmentsForAdmin() {
		List<AppointmentService.AppointmentResponse> response = appointmentService.listAppointmentsForAdmin();
		return ResponseEntity.ok(ApiResponse.ok("Appointments fetched", response));
	}

	@GetMapping({ "/admin/appointments/{appointmentId}", "/reception/appointments/{appointmentId}" })
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> getAppointmentForAdmin(@PathVariable Long appointmentId) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.getAppointmentByIdForAdmin(appointmentId);
			return ResponseEntity.ok(ApiResponse.ok("Appointment fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping({ "/admin/appointments/{appointmentId}", "/reception/appointments/{appointmentId}" })
	public ResponseEntity<ApiResponse<AppointmentService.AppointmentResponse>> updateAppointmentForAdmin(
		@PathVariable Long appointmentId,
		@RequestBody AppointmentService.AdminUpdateAppointmentRequest request
	) {
		try {
			AppointmentService.AppointmentResponse response = appointmentService.updateAppointmentForAdmin(appointmentId, request);
			return ResponseEntity.ok(ApiResponse.ok("Appointment updated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
