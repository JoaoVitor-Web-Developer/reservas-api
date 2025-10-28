package com.reservas.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.LoginRequest;
import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.model.User;
import com.reservas.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

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
	@Autowired
	private UserService userService;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		registerRequest = new UserRegisterRequest();
		registerRequest.setName("Integration User");
		registerRequest.setEmail("integration@test.com");
		registerRequest.setPassword("password123");
		registerRequest.setCpf("98765432100");

		loginRequest = new LoginRequest();
		loginRequest.setEmail("integration@test.com");
		loginRequest.setPassword("password123");
	}

	@Test
	@DisplayName("POST /auth/register - Deve registrar usuário com sucesso e retornar 201 Created")
	void register_shouldCreateUserAndReturnCreated() throws Exception {
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isCreated())
		       .andExpect(jsonPath("$.email").value("integration@test.com"))
		       .andExpect(jsonPath("$.id").isNotEmpty())
		       .andExpect(jsonPath("$.password").doesNotExist());

		// Verifica no banco real (ou de teste) se o usuário foi criado
		User userInDb = userRepository.findByEmail("integration@test.com").orElseThrow();
		assertThat(userInDb).isNotNull();
		assertThat(userInDb.getName()).isEqualTo("Integration User");
		// Verifica se a senha foi hashada
		assertThat(passwordEncoder.matches("password123", userInDb.getPassword())).isTrue();
	}

	@Test
	@DisplayName("POST /auth/register - Deve retornar 400 Bad Request ao registrar com email duplicado")
	void register_shouldReturnBadRequest_whenEmailExists() throws Exception {
		userService.saveUser(registerRequest);

		// Tenta registrar DE NOVO com o mesmo email
		mockMvc.perform(post("/auth/register")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(registerRequest)))
		       .andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /auth/login - Deve retornar 200 OK e token JWT com credenciais válidas")
	void login_shouldReturnOkAndToken_whenCredentialsAreValid() throws Exception {
		//Cria o usuário primeiro
		userService.saveUser(registerRequest);

		mockMvc.perform(post("/auth/login")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(loginRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	@DisplayName("POST /auth/login - Deve retornar 401 Unauthorized com senha inválida")
	void login_shouldReturnUnauthorized_whenPasswordIsInvalid() throws Exception {
		// Cria o usuário
		userService.saveUser(registerRequest);
		// Modifica a senha no DTO de login
		loginRequest.setPassword("senhaErrada");

		mockMvc.perform(post("/auth/login")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(loginRequest)))
		       .andExpect(status().isUnauthorized());
	}
}