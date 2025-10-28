package com.reservas.api.controller;

import com.reservas.api.entities.dto.ClientResponse;
import com.reservas.api.entities.dto.ClientUpdateRequest;
import com.reservas.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
@Tag(name = "Cliente (perfil)", description = "Endpoiont para gerenciar o perfil do usuário (cliente)")
public class ClientController {

	private final ClientService clientService;

	@Operation(summary = "Obter Meu Perfil", description = "Retorna os dados do perfil (Cliente) do usuário autenticado.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Perfil encontrado", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
			@ApiResponse(responseCode = "404", description = "Perfil não encontrado para o usuário (raro, pode indicar inconsistência)", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/me")
	public ResponseEntity<ClientResponse>getMyProfile() {
		return ResponseEntity.ok(clientService.getMyProfile());
	}

	@Operation(summary = "Atualizar Meu Perfil", description = "Atualiza os dados do perfil (Cliente) do usuário autenticado. Envie apenas os campos que deseja alterar.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso", content = @Content(schema = @Schema(implementation = ClientResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos (ex: email de perfil duplicado)", content = @Content),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
			@ApiResponse(responseCode = "404", description = "Perfil não encontrado para o usuário", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PutMapping("/me")
	public ResponseEntity<ClientResponse>updateMyProfile(@Valid @RequestBody ClientUpdateRequest updateRequest) {
		return ResponseEntity.ok(clientService.updateMyProfile(updateRequest));
	}
}
