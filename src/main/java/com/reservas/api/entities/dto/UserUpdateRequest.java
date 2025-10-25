package com.reservas.api.entities.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
	private String name;
	private String email;
	private String phone;
	private String cpf;
}
