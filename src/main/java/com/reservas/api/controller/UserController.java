package com.reservas.api.controller;

import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest updateRequest) {
		return userService.updateUser(id, updateRequest);
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserResponse findById(@PathVariable UUID id) {
		return  userService.findById(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
	}
}
