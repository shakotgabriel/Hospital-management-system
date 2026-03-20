package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/overview")
	public ResponseEntity<ApiResponse<AnalyticsService.OverviewResponse>> getOverview(
		@RequestParam(defaultValue = "30") int days,
		@RequestParam(defaultValue = "5") int topDoctors
	) {
		try {
			AnalyticsService.OverviewResponse response = analyticsService.getOverview(days, topDoctors);
			return ResponseEntity.ok(ApiResponse.ok("Analytics overview generated", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
