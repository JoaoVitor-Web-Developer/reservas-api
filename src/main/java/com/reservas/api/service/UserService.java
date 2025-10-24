package com.reservas.api.service;

import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.entities.mapper.UserMapper;
import com.reservas.api.entities.model.User;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public List<UserResponse> listAll() {
		return userRepository.findAll().stream()
				.map(userMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		return userMapper.toResponse(user);
	}

	@Transactional
	public UserResponse saveUser(UserRegisterRequest userRegisterRequest) {
		if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
			throw new ResourceNotFoundException("User already exists: " + userRegisterRequest.getEmail());
		}

		if (userRepository.findByCpf(userRegisterRequest.getCpf()).isPresent()) {
			throw  new ResourceNotFoundException("There is already a user with this CPF: " + userRegisterRequest.getCpf());
		}

		User user = userMapper.toEntity(userRegisterRequest);

		user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));

		User savedUser = userRepository.save(user);

		return userMapper.toResponse(savedUser);
	}

	@Transactional
	public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest) {
		User userExisting = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

		userRepository.findByEmail(userUpdateRequest.getEmail())
		              .ifPresent(userWithEmail -> {
						  if (!userWithEmail.getId().equals(id)) {
								  throw new ResourceNotFoundException("There is already a user with this email: " + userUpdateRequest.getEmail());
							  }
		              });

		userMapper.toEntity(userUpdateRequest, userExisting);

		User savedUser = userRepository.save(userExisting);

		return userMapper.toResponse(savedUser);
	}

	@Transactional
	public void deleteUser(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

		if (reservationRepository.existsByUser((user))) {
			throw new BusinessException("Cannot delete user with existing reservations");
		}

		userRepository.delete(user);
	}
}
