package com.reservas.api.mapper;

import com.reservas.api.dto.UserRegisterRequest;
import com.reservas.api.dto.UserResponse;
import com.reservas.api.dto.UserUpdateRequest;
import com.reservas.api.model.User;
import org.springframework.stereotype.Component;

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
		user.setCreatedAt(LocalDateTime.now());
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

	public void toEntity(UserUpdateRequest request, User user) {
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		user.setPhone(request.getPhone());
		user.setCpf(request.getCpf());
	}
}
