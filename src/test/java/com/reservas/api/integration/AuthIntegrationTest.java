package com.reservas.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.LoginRequest;
import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.model.User;
import com.reservas.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private UserRegisterRequest registerRequest;
	private LoginRequest loginRequest;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();

		registerRequest = new UserRegisterRequest();
		registerRequest.setName("Integration User");
		registerRequest.setEmail("integration@test.com");
		registerRequest.setPassword("password123");
		registerRequest.setCpf("98765432100");
		registerRequest.setPhone("11991234567");

		loginRequest = new LoginRequest();
		loginRequest.setEmail("integration@test.com");
		loginRequest.setPassword("password123");
	}

	@Test
	void register_shouldCreateUserAndReturnOk() throws Exception {
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.email").value("integration@test.com"))
		       .andExpect(jsonPath("$.id").exists());

		User userInDb = userRepository.findByEmail("integration@test.com").orElseThrow();
		assertThat(userInDb).isNotNull();
		assertThat(userInDb.getEmail()).isEqualTo("integration@test.com");
		assertThat(passwordEncoder.matches("password123", userInDb.getPassword())).isTrue();
	}

	@Test
	void register_shouldReturnBadRequest_whenEmailExists() throws Exception {
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isOk());

		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isBadRequest());
	}

	@Test
	void login_shouldReturnOkAndToken_whenCredentialsAreValid() throws Exception {
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isOk());

		mockMvc.perform(post("/auth/login")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(loginRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void login_shouldReturnUnauthorized_whenPasswordIsInvalid() throws Exception {
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isOk());

		loginRequest.setPassword("senhaErrada");

		mockMvc.perform(post("/auth/login")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(loginRequest)))
		       .andExpect(status().isUnauthorized());
	}
}
