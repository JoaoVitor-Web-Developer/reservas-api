package com.reservas.api.entities.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClientResponse {

	private UUID id;
	private String name;
	private String email;
	private String phone;
	private String cpf;
	private LocalDateTime createdAt;
}
