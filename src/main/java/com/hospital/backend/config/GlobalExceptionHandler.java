package com.hospital.backend.config;

import com.hospital.backend.dto.ErrorResponse;
import com.hospital.backend.exception.AppointmentException;
import com.hospital.backend.exception.BadRequestException;
import com.hospital.backend.exception.DoctorUnavailableException;
import com.hospital.backend.exception.ResourceNotFoundException;
import com.hospital.backend.exception.UnauthorizedException;
import com.hospital.backend.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleValidationException(
		MethodArgumentNotValidException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			"Validation Failed",
			"One or more fields have validation errors"
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.add(new ErrorResponse.FieldError(
				error.getField(),
				error.getDefaultMessage(),
				error.getRejectedValue()
			));
		}
		errorResponse.setFieldErrors(fieldErrors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleBadRequestException(
		BadRequestException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			"Bad Request",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleValidationBusinessException(
		ValidationException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			"Validation Error",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
		ResourceNotFoundException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.NOT_FOUND.value(),
			"Not Found",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(AppointmentException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ErrorResponse> handleAppointmentException(
		AppointmentException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.CONFLICT.value(),
			"Appointment Conflict",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(DoctorUnavailableException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ErrorResponse> handleDoctorUnavailableException(
		DoctorUnavailableException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.CONFLICT.value(),
			"Doctor Unavailable",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleUnauthorizedException(
		UnauthorizedException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.FORBIDDEN.value(),
			"Forbidden",
			ex.getMessage()
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(
		AccessDeniedException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.FORBIDDEN.value(),
			"Access Denied",
			"You do not have permission to access this resource"
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
		NoHandlerFoundException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.NOT_FOUND.value(),
			"Endpoint Not Found",
			"The requested endpoint does not exist"
		);
		errorResponse.setPath(ex.getRequestURL());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
		HttpMediaTypeNotSupportedException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
			"Unsupported Media Type",
			"Use Content-Type: application/json"
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleUnreadableMessage(
		HttpMessageNotReadableException ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			"Malformed JSON",
			"Request body is invalid JSON"
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ErrorResponse> handleGlobalException(
		Exception ex,
		WebRequest request
	) {
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"Internal Server Error",
			"An unexpected error occurred. Please try again later."
		);
		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		ex.printStackTrace();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
