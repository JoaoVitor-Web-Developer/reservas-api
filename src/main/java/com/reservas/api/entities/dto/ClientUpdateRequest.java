package com.reservas.api.entities.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientUpdateRequest {
	@Size(min = 2, message = "Name must have at least 2 characters")
	private String name;

	@Email(message = "Email must be valid")
	private String email;

	private String phone;
	private String cpf;
}
