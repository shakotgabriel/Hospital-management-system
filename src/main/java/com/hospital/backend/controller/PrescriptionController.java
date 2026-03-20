package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.PrescriptionService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PrescriptionController {

	private final PrescriptionService prescriptionService;

	public PrescriptionController(PrescriptionService prescriptionService) {
		this.prescriptionService = prescriptionService;
	}

	@PostMapping("/doctors/prescriptions")
	public ResponseEntity<ApiResponse<PrescriptionService.PrescriptionResponse>> createMyPrescription(
		Principal principal,
		@RequestBody PrescriptionService.CreatePrescriptionRequest request
	) {
		try {
			PrescriptionService.PrescriptionResponse response = prescriptionService.createMyPrescription(principal.getName(), request);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Prescription created", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/doctors/prescriptions/{prescriptionId}")
	public ResponseEntity<ApiResponse<PrescriptionService.PrescriptionResponse>> updateMyPrescription(
		Principal principal,
		@PathVariable Long prescriptionId,
		@RequestBody PrescriptionService.UpdatePrescriptionRequest request
	) {
		try {
			PrescriptionService.PrescriptionResponse response = prescriptionService.updateMyPrescription(
				principal.getName(),
				prescriptionId,
				request
			);
			return ResponseEntity.ok(ApiResponse.ok("Prescription updated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/doctors/prescriptions")
	public ResponseEntity<ApiResponse<List<PrescriptionService.PrescriptionResponse>>> listMyPrescriptions(Principal principal) {
		List<PrescriptionService.PrescriptionResponse> response = prescriptionService.listMyPrescriptions(principal.getName());
		return ResponseEntity.ok(ApiResponse.ok("Prescriptions fetched", response));
	}

	@GetMapping("/doctors/prescriptions/{prescriptionId}")
	public ResponseEntity<ApiResponse<PrescriptionService.PrescriptionResponse>> getMyPrescription(
		Principal principal,
		@PathVariable Long prescriptionId
	) {
		try {
			PrescriptionService.PrescriptionResponse response = prescriptionService.getMyPrescriptionById(
				principal.getName(),
				prescriptionId
			);
			return ResponseEntity.ok(ApiResponse.ok("Prescription fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/admin/prescriptions")
	public ResponseEntity<ApiResponse<List<PrescriptionService.PrescriptionResponse>>> listPrescriptionsForAdmin(
		@RequestParam(required = false) Long patientId,
		@RequestParam(required = false) Long doctorId,
		@RequestParam(required = false) Long appointmentId
	) {
		List<PrescriptionService.PrescriptionResponse> response = prescriptionService.listPrescriptionsForAdmin(
			patientId,
			doctorId,
			appointmentId
		);
		return ResponseEntity.ok(ApiResponse.ok("Prescriptions fetched", response));
	}

	@GetMapping("/admin/prescriptions/{prescriptionId}")
	public ResponseEntity<ApiResponse<PrescriptionService.PrescriptionResponse>> getPrescriptionForAdmin(
		@PathVariable Long prescriptionId
	) {
		try {
			PrescriptionService.PrescriptionResponse response = prescriptionService.getPrescriptionByIdForAdmin(prescriptionId);
			return ResponseEntity.ok(ApiResponse.ok("Prescription fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}
}
