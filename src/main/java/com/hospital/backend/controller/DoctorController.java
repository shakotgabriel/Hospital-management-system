package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.DoctorService;
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
public class DoctorController {

	private final DoctorService doctorService;

	public DoctorController(DoctorService doctorService) {
		this.doctorService = doctorService;
	}

	@GetMapping("/doctors/me")
	public ResponseEntity<ApiResponse<DoctorService.DoctorResponse>> getMyDoctorProfile(Principal principal) {
		try {
			DoctorService.DoctorResponse response = doctorService.getMyProfile(principal.getName());
			return ResponseEntity.ok(ApiResponse.ok("Doctor profile fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/doctors/me")
	public ResponseEntity<ApiResponse<DoctorService.DoctorResponse>> upsertMyDoctorProfile(
		Principal principal,
		@RequestBody DoctorService.UpsertMyDoctorProfileRequest request
	) {
		try {
			DoctorService.DoctorResponse response = doctorService.upsertMyProfile(principal.getName(), request);
			return ResponseEntity.ok(ApiResponse.ok("Doctor profile saved", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping({ "/admin/doctors", "/reception/doctors" })
	public ResponseEntity<ApiResponse<DoctorService.PagedDoctorResponse>> listDoctors(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		DoctorService.PagedDoctorResponse response = doctorService.listDoctors(page, size);
		return ResponseEntity.ok(ApiResponse.ok("Doctors fetched", response));
	}

	@GetMapping({ "/admin/doctors/{doctorId}", "/reception/doctors/{doctorId}" })
	public ResponseEntity<ApiResponse<DoctorService.DoctorResponse>> getDoctorById(@PathVariable Long doctorId) {
		try {
			DoctorService.DoctorResponse response = doctorService.getDoctorById(doctorId);
			return ResponseEntity.ok(ApiResponse.ok("Doctor fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PostMapping({ "/admin/doctors", "/reception/doctors" })
	public ResponseEntity<ApiResponse<DoctorService.DoctorResponse>> createDoctor(
		@RequestBody DoctorService.AdminCreateDoctorRequest request
	) {
		try {
			DoctorService.DoctorResponse response = doctorService.createDoctor(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Doctor created", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping({ "/admin/doctors/{doctorId}", "/reception/doctors/{doctorId}" })
	public ResponseEntity<ApiResponse<DoctorService.DoctorResponse>> updateDoctor(
		@PathVariable Long doctorId,
		@RequestBody DoctorService.AdminUpdateDoctorRequest request
	) {
		try {
			DoctorService.DoctorResponse response = doctorService.updateDoctor(doctorId, request);
			return ResponseEntity.ok(ApiResponse.ok("Doctor updated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
