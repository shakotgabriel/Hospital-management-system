package com.hospital.backend.exception;

public class DoctorUnavailableException extends RuntimeException {

	public DoctorUnavailableException(String message) {
		super(message);
	}

	public DoctorUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
