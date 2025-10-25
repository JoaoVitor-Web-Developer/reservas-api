package com.reservas.api.controller;

import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.entities.dto.UserUpdateRequest;
import com.reservas.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User (Clientes)", description = "Endpoints para gerenciar usuários (Clientes)")
public class UserController {

	private final UserService userService;

	@Operation(summary = "Atualizar Usuário", description = "Atualiza os dados de um usuário existente. Requer autenticação. Usuários normais só podem atualizar a si mesmos, ADMINs podem atualizar qualquer um.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos na requisição (ex: email duplicado)", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado (tentando atualizar outro usuário sem ser ADMIN)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID fornecido", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest updateRequest) {
		return userService.updateUser(id, updateRequest);
	}

	@Operation(summary = "Buscar Usuário por ID", description = "Retorna os dados de um usuário específico. Requer autenticação. ADMINs podem ver qualquer usuário, USERs só podem ver a si mesmos.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "403", description = "Acesso negado (tentando ver outro usuário sem ser ADMIN)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID fornecido", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserResponse findById(@PathVariable UUID id) {
		return  userService.findById(id);
	}

	@Operation(summary = "Deletar Usuário", description = "Deleta um usuário existente. Requer autenticação. Usuários normais só podem deletar a si mesmos, ADMINs podem deletar qualquer um.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso", content = @Content),
			@ApiResponse(responseCode = "400", description = "Não é possível deletar (ex: usuário possui reservas ativas)", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado (tentando deletar outro usuário sem ser ADMIN)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID fornecido", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
	}
}
