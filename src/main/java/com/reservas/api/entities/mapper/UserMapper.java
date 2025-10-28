package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.entities.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final ClientMapper clientMapper;

	public UserResponse toResponse(User user) {
		if (user == null) return null;
		UserResponse dto = new UserResponse();
		dto.setId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setRole(user.getRole());
		dto.setClient(clientMapper.toResponse(user.getClient()));
		return dto;
	}
}
