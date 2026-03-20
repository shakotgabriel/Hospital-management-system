package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.PatientService;
import java.security.Principal;
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
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@GetMapping("/patients/me")
	public ResponseEntity<ApiResponse<PatientService.PatientResponse>> getMyPatientProfile(Principal principal) {
		try {
			PatientService.PatientResponse response = patientService.getMyProfile(principal.getName());
			return ResponseEntity.ok(ApiResponse.ok("Patient profile fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/patients/me")
	public ResponseEntity<ApiResponse<PatientService.PatientResponse>> upsertMyPatientProfile(
		Principal principal,
		@RequestBody PatientService.UpsertMyPatientProfileRequest request
	) {
		try {
			PatientService.PatientResponse response = patientService.upsertMyProfile(principal.getName(), request);
			return ResponseEntity.ok(ApiResponse.ok("Patient profile saved", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/admin/patients")
	public ResponseEntity<ApiResponse<PatientService.PagedPatientResponse>> listPatients(
		@RequestParam(required = false) String search,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		PatientService.PagedPatientResponse response = patientService.listPatients(search, page, size);
		return ResponseEntity.ok(ApiResponse.ok("Patients fetched", response));
	}

	@GetMapping("/admin/patients/{patientId}")
	public ResponseEntity<ApiResponse<PatientService.PatientResponse>> getPatientById(@PathVariable Long patientId) {
		try {
			PatientService.PatientResponse response = patientService.getPatientById(patientId);
			return ResponseEntity.ok(ApiResponse.ok("Patient fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PostMapping("/admin/patients")
	public ResponseEntity<ApiResponse<PatientService.PatientResponse>> createPatient(
		@RequestBody PatientService.AdminCreatePatientRequest request
	) {
		try {
			PatientService.PatientResponse response = patientService.createPatient(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Patient created", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/admin/patients/{patientId}")
	public ResponseEntity<ApiResponse<PatientService.PatientResponse>> updatePatient(
		@PathVariable Long patientId,
		@RequestBody PatientService.AdminUpdatePatientRequest request
	) {
		try {
			PatientService.PatientResponse response = patientService.updatePatient(patientId, request);
			return ResponseEntity.ok(ApiResponse.ok("Patient updated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
