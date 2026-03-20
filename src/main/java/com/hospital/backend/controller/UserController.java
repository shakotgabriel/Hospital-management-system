package com.hospital.backend.controller;

import com.hospital.backend.dto.ApiResponse;
import com.hospital.backend.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users/me")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> getCurrentUser(Principal principal) {
		try {
			UserService.UserResponse user = userService.getCurrentUser(principal.getName());
			return ResponseEntity.ok(ApiResponse.ok("User profile fetched", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/users/me")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> updateCurrentUser(
		Principal principal,
		@RequestBody UserService.UpdateProfileRequest request
	) {
		try {
			UserService.UserResponse user = userService.updateCurrentUser(principal.getName(), request);
			return ResponseEntity.ok(ApiResponse.ok("User profile updated", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@GetMapping("/admin/users")
	public ResponseEntity<ApiResponse<List<UserService.UserResponse>>> listUsers() {
		return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.listUsers()));
	}

	@PostMapping("/admin/users")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> createUser(
		@Valid @RequestBody UserService.AdminCreateUserRequest request
	) {
		try {
			UserService.UserResponse user = userService.createUser(request);
			return ResponseEntity.ok(ApiResponse.ok("User created", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PutMapping("/admin/users/{userId}")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> updateUser(
		@PathVariable Long userId,
		@RequestBody UserService.AdminUpdateUserRequest request
	) {
		try {
			UserService.UserResponse user = userService.updateUser(userId, request);
			return ResponseEntity.ok(ApiResponse.ok("User updated", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/admin/users/{userId}/activate")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> activateUser(@PathVariable Long userId) {
		try {
			UserService.UserResponse user = userService.setUserActiveStatus(userId, true);
			return ResponseEntity.ok(ApiResponse.ok("User activated", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}

	@PatchMapping("/admin/users/{userId}/deactivate")
	public ResponseEntity<ApiResponse<UserService.UserResponse>> deactivateUser(@PathVariable Long userId) {
		try {
			UserService.UserResponse user = userService.setUserActiveStatus(userId, false);
			return ResponseEntity.ok(ApiResponse.ok("User deactivated", user));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
		}
	}
}
