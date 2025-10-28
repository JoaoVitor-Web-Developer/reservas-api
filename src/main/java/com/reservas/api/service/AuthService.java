package com.reservas.api.service;

import com.reservas.api.entities.dto.LoginRequest;
import com.reservas.api.entities.dto.LoginResponse;
import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.mapper.ClientMapper;
import com.reservas.api.entities.mapper.UserMapper;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.repository.ClientRepository;
import com.reservas.api.repository.UserRepository;
import com.reservas.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final ClientRepository clientRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final ClientMapper clientMapper;

	@Transactional
	public UserResponse register(UserRegisterRequest registerRequest) {

		if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
			throw new BusinessException("User already exists: " + registerRequest.getEmail());
		}

		if (clientRepository.findByCpf(registerRequest.getCpf()).isPresent()) {
			throw new BusinessException("There is already a client with this CPF: " + registerRequest.getCpf());
		}

		User newUser = new User();
		newUser.setEmail(registerRequest.getEmail());
		newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

		Client newClient = clientMapper.fromRegisterRequest(registerRequest);

		newUser.setClient(newClient);
		newClient.setUser(newUser);

		User savedUser = userRepository.save(newUser);

		return userMapper.toResponse(savedUser);
	}

	public LoginResponse login(LoginRequest loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequest.getEmail().toLowerCase(),
							loginRequest.getPassword()
					)
			);

			User userDetails = (User) authentication.getPrincipal();
			String token = jwtUtil.generateToken(userDetails);
			long expiresIn = jwtUtil.getExpirationMs();

			UserResponse userResponse = userMapper.toResponse(userDetails);

			return new LoginResponse(token, expiresIn, userResponse);
		} catch(BadCredentialsException e) {
			throw new BadCredentialsException("Invalid email or password");
		} catch(Exception e) {
			throw new BadCredentialsException("Authentication failed");
		}
	}
}
