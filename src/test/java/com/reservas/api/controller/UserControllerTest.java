package com.reservas.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ForbiddenException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	private UUID userId;
	private UserUpdateRequest updateRequest;
	private UserResponse userResponse;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();

		updateRequest = new UserUpdateRequest();
		updateRequest.setName("Updated Name");
		updateRequest.setEmail("update@email.com");

		userResponse = new UserResponse();
		userResponse.setId(userId);
		userResponse.setName("Updated Name");
		userResponse.setEmail("update@email.com");
	}

	// --- Testes para updateUser ---

	@Test
	@DisplayName("PUT /user/{id} - Deve retornar 200 OK quando atualização é válida")
	@WithMockUser
	void updateUser_shouldReturnOk_whenDataIsValid() throws Exception {
		// Simula o service retornando o usuário atualizado
		when(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(userResponse);

		mockMvc.perform(put("/user/{id}", userId)
		                                          .contentType(MediaType.APPLICATION_JSON)
		                                          .content(objectMapper.writeValueAsString(updateRequest))
		               )
		       .andExpect(status().isOk())
		       .andExpect(content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(jsonPath("$.id").value(userId.toString()))
		       .andExpect(jsonPath("$.name").value("Updated Name"));
	}

	@Test
	@DisplayName("PUT /user/{id} - Deve retornar 400 Bad Request quando dados são inválidos")
	@WithMockUser
	void updateUser_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
		updateRequest.setEmail("email-invalido");

		mockMvc.perform(put("/user/{id}", userId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(updateRequest)))
		       .andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /user/{id} - Deve retornar 404 Not Found quando usuário não existe")
	@WithMockUser
	void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
		// Simula o service lançando a exceção
		when(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
				.thenThrow(new ResourceNotFoundException("User not found: " + userId));

		mockMvc.perform(put("/user/{id}", userId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(updateRequest)))
		       .andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("PUT /user/{id} - Deve retornar 403 Forbidden quando usuário tenta atualizar outro")
	@WithMockUser
	void updateUser_shouldReturnForbidden_whenUserUpdatesAnotherUser() throws Exception {
		UUID otherUserId = UUID.randomUUID();

		// Simula o service lançando ForbiddenException quando o ID não bate
		when(userService.updateUser(eq(otherUserId), any(UserUpdateRequest.class)))
				.thenThrow(new ForbiddenException("Acesso negado."));


		mockMvc.perform(put("/user/{id}", otherUserId)
		                                               .contentType(MediaType.APPLICATION_JSON)
		                                               .content(objectMapper.writeValueAsString(updateRequest)))
		       .andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PUT /user/{id} - Deve retornar 401 Unauthorized quando não autenticado")
	void updateUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(put("/user/{id}", userId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(updateRequest)))
		       .andExpect(status().isUnauthorized());
	}


	// --- Testes para findById ---

	@Test
	@DisplayName("GET /user/{id} - Deve retornar 200 OK e usuário quando busca a si mesmo")
	@WithMockUser
	void findById_shouldReturnOkAndUser_whenFindingSelf() throws Exception {
		when(userService.findById(userId)).thenReturn(userResponse);

		mockMvc.perform(get("/user/{id}", userId))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.id").value(userId.toString()));
	}

	@Test
	@DisplayName("GET /user/{id} - Deve retornar 200 OK e usuário quando ADMIN busca outro")
	@WithMockUser(roles = "ADMIN")
	void findById_shouldReturnOkAndUser_whenAdminFindingOther() throws Exception {
		UUID targetUserId = UUID.randomUUID();
		UserResponse targetUserResponse = new UserResponse();
		targetUserResponse.setId(targetUserId);
		targetUserResponse.setName("Target User");


		when(userService.findById(targetUserId)).thenReturn(targetUserResponse);


		mockMvc.perform(get("/user/{id}", targetUserId))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.id").value(targetUserId.toString()));
	}


	@Test
	@DisplayName("GET /user/{id} - Deve retornar 403 Forbidden quando USER busca outro")
	@WithMockUser
	void findById_shouldReturnForbidden_whenUserFindingOther() throws Exception {
		UUID otherUserId = UUID.randomUUID();
		when(userService.findById(otherUserId)).thenThrow(new ForbiddenException("Acesso negado."));

		mockMvc.perform(get("/user/{id}", otherUserId))
		       .andExpect(status().isForbidden());
	}

	// (Teste para 404 Not Found no GET /user/{id})
	// (Teste para 401 Unauthorized no GET /user/{id})
	// --- Testes para deleteUser ---
	@Test
	@DisplayName("DELETE /user/{id} - Deve retornar 204 No Content quando deleta a si mesmo")
	@WithMockUser
	void deleteUser_shouldReturnNoContent_whenDeletingSelf() throws Exception {
		// Simula o service não lançando exceção
		doNothing().when(userService).deleteUser(userId);

		mockMvc.perform(delete("/user/{id}", userId))
		       .andExpect(status().isNoContent()); // Espera 204
	}

	@Test
	@DisplayName("DELETE /user/{id} - Deve retornar 403 Forbidden quando USER deleta outro")
	@WithMockUser
	void deleteUser_shouldReturnForbidden_whenUserDeletingOther() throws Exception {
		UUID otherUserId = UUID.randomUUID();
		doThrow(new ForbiddenException("Acesso negado.")).when(userService).deleteUser(otherUserId);

		mockMvc.perform(delete("/user/{id}", otherUserId))
		       .andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("DELETE /user/{id} - Deve retornar 400 Bad Request quando não pode deletar (ex: reservas)")
	@WithMockUser
	void deleteUser_shouldReturnBadRequest_whenDeletionNotAllowed() throws Exception {
		// Arrange
		doThrow(new BusinessException("Cannot delete user...")).when(userService).deleteUser(userId);

		mockMvc.perform(delete("/user/{id}", userId))
		       .andExpect(status().isBadRequest());
	}
}