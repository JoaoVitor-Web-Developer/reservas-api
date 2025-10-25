package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.entities.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserMapper {

	public User toEntity(UserRegisterRequest request) {
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		user.setPhone(request.getPhone());
		user.setCpf(request.getCpf());
		user.setCreatedAt(LocalDate.now());
		return user;
	}

	public UserResponse toResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setPhone(user.getPhone());
		response.setCpf(user.getCpf());
		response.setCreatedAt(user.getCreatedAt());
		return response;
	}

	public void updateEntityFromRequest(UserUpdateRequest request, User user) {
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setCpf(request.getCpf());
	}
}
