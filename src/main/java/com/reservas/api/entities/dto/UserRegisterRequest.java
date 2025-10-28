package com.reservas.api.entities.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotBlank(message = "Email is required")
	@Email
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must have at least 8 characters")
	private String password;

	@NotBlank(message = "CPF is required")
	private String cpf;

	@NotBlank(message = "Phone is required")
	private String phone;
}
