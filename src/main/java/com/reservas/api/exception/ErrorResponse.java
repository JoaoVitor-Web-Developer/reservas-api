package com.reservas.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	private int status;
	private String message;
	private List<String> errors;

	public ErrorResponse(HttpStatus status, String message, List<String> errors) {
		this.status = status.value();
		this.message = message;
		this.errors = errors;
	}

	public ErrorResponse(HttpStatus status, String message) {
		this.status = status.value();
		this.message = message;
		this.errors = null;
	}
}