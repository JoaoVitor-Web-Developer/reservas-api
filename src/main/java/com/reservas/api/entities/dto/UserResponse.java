package com.reservas.api.entities.dto;

import com.reservas.api.entities.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
	private UUID id;
	private String email;
	private Role role;
}
