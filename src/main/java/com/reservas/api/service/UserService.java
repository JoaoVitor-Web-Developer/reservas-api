package com.reservas.api.service;

import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ForbiddenException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.entities.mapper.UserMapper;
import com.reservas.api.entities.model.User;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
	public UserResponse findById(UUID id) {
		checkIfIsSameUserOrAdmin(id);
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		return userMapper.toResponse(user);
	}

	@Transactional
	public UserResponse saveUser(UserRegisterRequest userRegisterRequest) {
		if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
			throw new BusinessException("User already exists: " + userRegisterRequest.getEmail());
		}

		if (userRegisterRequest.getCpf() != null && !userRegisterRequest.getCpf().isEmpty() &&
				userRepository.findByCpf(userRegisterRequest.getCpf()).isPresent()) {
			throw new BusinessException("There is already a user with this CPF: " + userRegisterRequest.getCpf());
		}

		User user = userMapper.toEntity(userRegisterRequest);
		user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));

		if (user.getCreatedAt() == null) {
			user.setCreatedAt(LocalDate.now());
		}

		User savedUser = userRepository.save(user);
		return userMapper.toResponse(savedUser);
	}

	@Transactional
	public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest) {
		checkIfIsSameUserOrAdmin(id);

		User userExisting = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

		userRepository.findByEmail(userUpdateRequest.getEmail())
		              .ifPresent(userWithEmail -> {
						  if (!userWithEmail.getId().equals(id)) {
								  throw new BusinessException("There is already a user with this email: " + userUpdateRequest.getEmail());
							  }
		              });

		userMapper.updateEntityFromRequest(userUpdateRequest, userExisting);

		User savedUser = userRepository.save(userExisting);

		return userMapper.toResponse(savedUser);
	}

	@Transactional
	public void deleteUser(UUID id) {
		checkIfIsSameUserOrAdmin(id);

		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

		if (reservationRepository.existsByUser((user))) {
			throw new BusinessException("Cannot delete user with existing reservations");
		}

		userRepository.delete(user);
	}
	
	@Transactional(readOnly = true)
	public UserResponse findByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		return userMapper.toResponse(user);
	}

	private void checkIfIsSameUserOrAdmin(UUID userId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
			throw new ForbiddenException("Unauthorized access. Authentication required.");
		}

		UserDetails principal = (UserDetails) authentication.getPrincipal();

		boolean isAdmin = principal.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_AMIN"));

		if (isAdmin) {
			return;
		}

		String loggedInUsername = principal.getUsername();

		User resourceUser = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (!loggedInUsername.equals(resourceUser.getUsername())) {
			throw new ForbiddenException("Access denied. You can only access or modify your own account.");
		}
	}
}
