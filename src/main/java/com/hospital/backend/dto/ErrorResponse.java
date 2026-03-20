package com.hospital.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	private int status;
	private String error;
	private String message;
	private String path;
	private OffsetDateTime timestamp;
	private List<FieldError> fieldErrors;

	public ErrorResponse() {
		this.timestamp = OffsetDateTime.now();
	}

	public ErrorResponse(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.timestamp = OffsetDateTime.now();
	}

	public ErrorResponse(int status, String error, String message, String path) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.timestamp = OffsetDateTime.now();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public static class FieldError {
		private String field;
		private String message;
		private Object rejectedValue;

		public FieldError(String field, String message, Object rejectedValue) {
			this.field = field;
			this.message = message;
			this.rejectedValue = rejectedValue;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Object getRejectedValue() {
			return rejectedValue;
		}

		public void setRejectedValue(Object rejectedValue) {
			this.rejectedValue = rejectedValue;
		}
	}
}
