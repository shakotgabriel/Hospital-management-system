package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.BillingService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BillingController {

	private final BillingService billingService;

	public BillingController(BillingService billingService) {
		this.billingService = billingService;
	}

	@GetMapping("/bills/me")
	public ResponseEntity<ApiResponse<List<BillingService.BillingResponse>>> listMyBills(Principal principal) {
		try {
			List<BillingService.BillingResponse> response = billingService.listMyBills(principal.getName());
			return ResponseEntity.ok(ApiResponse.ok("Bills fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/bills/me/{billingId}")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> getMyBill(
		Principal principal,
		@PathVariable Long billingId
	) {
		try {
			BillingService.BillingResponse response = billingService.getMyBill(principal.getName(), billingId);
			return ResponseEntity.ok(ApiResponse.ok("Bill fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PostMapping("/bills/me/{billingId}/payments")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> payMyBill(
		Principal principal,
		@PathVariable Long billingId,
		@RequestBody BillingService.RecordPaymentRequest request
	) {
		try {
			BillingService.BillingResponse response = billingService.recordMyPayment(principal.getName(), billingId, request);
			return ResponseEntity.ok(ApiResponse.ok("Payment recorded", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/admin/bills")
	public ResponseEntity<ApiResponse<List<BillingService.BillingResponse>>> listBillsForAdmin() {
		List<BillingService.BillingResponse> response = billingService.listBillsForAdmin();
		return ResponseEntity.ok(ApiResponse.ok("Bills fetched", response));
	}

	@GetMapping("/admin/bills/{billingId}")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> getBillForAdmin(@PathVariable Long billingId) {
		try {
			BillingService.BillingResponse response = billingService.getBillForAdmin(billingId);
			return ResponseEntity.ok(ApiResponse.ok("Bill fetched", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PostMapping("/admin/bills")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> createBillForAdmin(
		@RequestBody BillingService.AdminCreateBillRequest request
	) {
		try {
			BillingService.BillingResponse response = billingService.createBillForAppointment(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Bill created", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/admin/bills/{billingId}")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> updateBillForAdmin(
		@PathVariable Long billingId,
		@RequestBody BillingService.AdminUpdateBillRequest request
	) {
		try {
			BillingService.BillingResponse response = billingService.updateBillForAdmin(billingId, request);
			return ResponseEntity.ok(ApiResponse.ok("Bill updated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/admin/bills/{billingId}/issue")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> issueBillForAdmin(@PathVariable Long billingId) {
		try {
			BillingService.BillingResponse response = billingService.issueBillForAdmin(billingId);
			return ResponseEntity.ok(ApiResponse.ok("Bill issued", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/admin/bills/{billingId}/void")
	public ResponseEntity<ApiResponse<BillingService.BillingResponse>> voidBillForAdmin(@PathVariable Long billingId) {
		try {
			BillingService.BillingResponse response = billingService.voidBillForAdmin(billingId);
			return ResponseEntity.ok(ApiResponse.ok("Bill voided", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
