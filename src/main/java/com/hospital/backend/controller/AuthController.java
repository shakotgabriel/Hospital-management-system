package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.dto.LoginRequest;
import com.hospital.backend.dto.SignupRequest;
import com.hospital.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<AuthService.AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
		try {
			AuthService.AuthResponse response = authService.signup(request);
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Signup successful", response));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthService.AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		try {
			AuthService.AuthResponse response = authService.login(request);
			return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.error(ex.getMessage()));
		}
	}
}
