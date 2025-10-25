package com.reservas.api.service;

import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.mapper.UserMapper;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ForbiddenException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;


	@InjectMocks
	private UserService userService;

	private UserRegisterRequest registerRequest;
	private UserUpdateRequest updateRequest;
	private User user;
	private User adminUser;
	private UserResponse userResponse;
	private UUID userId;
	private UUID otherUserId;

	@BeforeEach
	void setUp() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);


		userId = UUID.randomUUID();
		otherUserId = UUID.randomUUID();

		registerRequest = new UserRegisterRequest();
		registerRequest.setName("Test User");
		registerRequest.setEmail("test@email.com");
		registerRequest.setPassword("password123");
		registerRequest.setCpf("12345678900");
		registerRequest.setPhone("11999998888");

		updateRequest = new UserUpdateRequest();
		updateRequest.setName("Updated Name");
		updateRequest.setEmail("updated@email.com");
		updateRequest.setPhone("11888887777");

		user = new User();
		user.setId(userId);
		user.setName("Test User");
		user.setEmail("test@email.com");
		user.setPassword("hashedPassword");
		user.setCpf("12345678900");
		user.setCreatedAt(LocalDate.now());
		user.setRole(Role.USER);
		user.setEnabled(true);
		user.setAccountNonLocked(true);
		user.setAccountNonExpired(true);
		user.setCredentialsNonExpired(true);

		adminUser = new User();
		adminUser.setId(UUID.randomUUID());
		adminUser.setEmail("admin@email.com");
		adminUser.setRole(Role.ADMIN);
		adminUser.setEnabled(true);
		adminUser.setAccountNonLocked(true);
		adminUser.setAccountNonExpired(true);
		adminUser.setCredentialsNonExpired(true);


		userResponse = new UserResponse();
		userResponse.setId(userId);
		userResponse.setName("Test User");
		userResponse.setEmail("test@email.com");
		userResponse.setCpf("12345678900");
		userResponse.setPhone("11999998888");
	}

	// --- Testes para saveUser ---

	@Test
	@DisplayName("Deve salvar usuário com sucesso quando dados são válidos")
	void saveUser_shouldSaveUser_whenDataIsValid() {
		// Simula que não encontrou email ou cpf existente
		when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
		when(userRepository.findByCpf(registerRequest.getCpf())).thenReturn(Optional.empty());
		// Simula o mapper convertendo o DTO para Entidade
		when(userMapper.toEntity(registerRequest)).thenReturn(user);
		// Simula o password encoder
		when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
		// Simula o save do repositório (retorna o usuário com ID e senha hash)
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User userToSave = invocation.getArgument(0);
			userToSave.setId(userId);
			userToSave.setPassword("hashedPassword");
			userToSave.setCreatedAt(LocalDate.now());
			if (userToSave.getRole() == null) userToSave.setRole(Role.USER);
			return userToSave;
		});

		when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

		UserResponse savedUserResponse = userService.saveUser(registerRequest);

		assertThat(savedUserResponse).isNotNull();
		assertThat(savedUserResponse.getId()).isEqualTo(userId);
		assertThat(savedUserResponse.getEmail()).isEqualTo(registerRequest.getEmail());

		verify(userRepository).findByEmail(registerRequest.getEmail());
		verify(userRepository).findByCpf(registerRequest.getCpf());
		verify(passwordEncoder).encode(registerRequest.getPassword());
		verify(userRepository).save(any(User.class));
		verify(userMapper).toResponse(any(User.class));
	}

	@Test
	@DisplayName("Deve lançar BusinessException ao salvar usuário com email existente")
	void saveUser_shouldThrowBusinessException_whenEmailExists() {
		when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.saveUser(registerRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("User already exists");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Deve lançar BusinessException ao salvar usuário com CPF existente")
	void saveUser_shouldThrowBusinessException_whenCpfExists() {

		when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
		when(userRepository.findByCpf(registerRequest.getCpf())).thenReturn(Optional.of(user)); // Simula CPF encontrado

		assertThatThrownBy(() -> userService.saveUser(registerRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("There is already a user with this CPF");

		verify(userRepository, never()).save(any(User.class));
	}


	// --- Testes para updateUser ---

	@Test
	@DisplayName("Deve atualizar usuário com sucesso quando dados são válidos e é o próprio usuário")
	void updateUser_shouldUpdateUser_whenDataIsValidAndIsSameUser() {
		// Simula o usuário logado (o próprio usuário)
		when(authentication.getPrincipal()).thenReturn(user);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		// Simula validação de email (não encontrou outro com o mesmo email)
		when(userRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.empty());
		// Simula o save (poderia retornar o user modificado)
		when(userRepository.save(any(User.class))).thenReturn(user);
		// Simula o mapper de resposta
		when(userMapper.toResponse(user)).thenReturn(userResponse);

		UserResponse updatedResponse = userService.updateUser(userId, updateRequest);

		assertThat(updatedResponse).isNotNull();

		verify(userMapper).updateEntityFromRequest(updateRequest, user);
		verify(userRepository).save(user);
	}

	@Test
	@DisplayName("Deve atualizar usuário com sucesso quando é ADMIN atualizando outro usuário")
	void updateUser_shouldUpdateUser_whenIsAdminUpdatingOtherUser() {
		// Simula o ADMIN logado
		when(authentication.getPrincipal()).thenReturn(adminUser);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(user);
		when(userMapper.toResponse(user)).thenReturn(userResponse);

		UserResponse updatedResponse = userService.updateUser(userId, updateRequest);

		assertThat(updatedResponse).isNotNull();
		verify(userMapper).updateEntityFromRequest(updateRequest, user);
		verify(userRepository).save(user);
	}


	@Test
	@DisplayName("Deve lançar ForbiddenException ao atualizar outro usuário sem ser ADMIN")
	void updateUser_shouldThrowForbiddenException_whenUserUpdatesOtherUserAndIsNotAdmin() {
		User loggedInUser = new User();
		loggedInUser.setId(otherUserId);
		loggedInUser.setEmail("other@email.com");
		loggedInUser.setRole(Role.USER);
		loggedInUser.setEnabled(true);
		loggedInUser.setAccountNonLocked(true);
		loggedInUser.setAccountNonExpired(true);
		loggedInUser.setCredentialsNonExpired(true);


		when(authentication.getPrincipal()).thenReturn(loggedInUser);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
		                                                                        .isInstanceOf(ForbiddenException.class)
		                                                                        .hasMessageContaining("Acesso negado");

		verify(userRepository, never()).save(any(User.class));
		verify(userMapper, never()).updateEntityFromRequest(any(), any());
	}


	@Test
	@DisplayName("Deve lançar ResourceNotFoundException ao atualizar usuário inexistente")
	void updateUser_shouldThrowResourceNotFoundException_whenUserNotFound() {
		// (Simular usuário logado - necessário para passar pelo checkIfIsSameUserOrAdmin)
		when(authentication.getPrincipal()).thenReturn(user); // Simula o próprio usuário
		when(userRepository.findById(userId)).thenReturn(Optional.empty()); // NÃO encontrou

		assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}


	// --- Testes para deleteUser ---

	@Test
	@DisplayName("Deve deletar usuário com sucesso quando é o próprio usuário e não tem reservas")
	void deleteUser_shouldDeleteUser_whenIsSameUserAndNoReservations() {
		when(authentication.getPrincipal()).thenReturn(user);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(reservationRepository.existsByUser(user)).thenReturn(false);
		// Configura o doNothing para o método void delete
		doNothing().when(userRepository).delete(user);


		// Chama o método e verifica se não lança exceção
		assertThatCode(() -> userService.deleteUser(userId)).doesNotThrowAnyException();

		// Verifica se o delete foi chamado UMA vez com o objeto user correto
		verify(userRepository, times(1)).delete(user);
	}


	@Test
	@DisplayName("Deve lançar BusinessException ao deletar usuário com reservas existentes")
	void deleteUser_shouldThrowBusinessException_whenUserHasReservations() {
		when(authentication.getPrincipal()).thenReturn(user);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(reservationRepository.existsByUser(user)).thenReturn(true); // TEM reservas

		assertThatThrownBy(() -> userService.deleteUser(userId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Cannot delete user with existing reservations");

		verify(userRepository, never()).delete(any(User.class));
	}


	// (Testes para ADMIN deletando outro usuário)
	// (Testes para USER tentando deletar outro usuário -> ForbiddenException)
	// (Testes para deletar usuário inexistente -> ResourceNotFoundException)
	// (Testes para findById --- (considerando a lógica de segurança))

	@Test
	@DisplayName("Deve retornar usuário quando busca a si mesmo")
	void findById_shouldReturnUser_whenFindingSelf() {
		when(authentication.getPrincipal()).thenReturn(user);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toResponse(user)).thenReturn(userResponse);

		UserResponse foundResponse = userService.findById(userId);

		assertThat(foundResponse).isNotNull();
		assertThat(foundResponse.getId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("Deve retornar usuário quando ADMIN busca outro usuário")
	void findById_shouldReturnUser_whenAdminFindingOther() {
		when(authentication.getPrincipal()).thenReturn(adminUser);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toResponse(user)).thenReturn(userResponse);

		UserResponse foundResponse = userService.findById(userId);

		assertThat(foundResponse).isNotNull();
		assertThat(foundResponse.getId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("Deve lançar ForbiddenException quando USER busca outro usuário")
	void findById_shouldThrowForbiddenException_whenUserFindingOther() {
		User loggedInUser = new User();
		loggedInUser.setId(otherUserId);
		loggedInUser.setEmail("other@email.com");
		loggedInUser.setRole(Role.USER);
		loggedInUser.setEnabled(true);
		loggedInUser.setAccountNonLocked(true);
		loggedInUser.setAccountNonExpired(true);
		loggedInUser.setCredentialsNonExpired(true);


		when(authentication.getPrincipal()).thenReturn(loggedInUser);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.findById(userId))
		                                                       .isInstanceOf(ForbiddenException.class)
		                                                       .hasMessageContaining("Acesso negado");
	}

	@Test
	@DisplayName("Deve lançar ResourceNotFoundException ao buscar usuário inexistente")
	void findById_shouldThrowResourceNotFoundException_whenUserNotFound() {
		when(authentication.getPrincipal()).thenReturn(user);
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.findById(userId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
	}

}