package com.reservas.api.entities.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
	private UUID id;
	private String name;
	private String email;
	private String phone;
	private String cpf;
	private LocalDate createdAt;
}
