package com.reservas.api.controller;

import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Endpoints para criar e gerenciar reservas")
public class ReservationController {

	private final ReservationService reservationService;

	@Operation(summary = "Criar Nova Reserva", description = "Cria uma nova reserva para o usuário autenticado na locação e período especificados. Requer autenticação.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Reserva criada com sucesso", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos (datas, duração, etc) ou Locação indisponível", content = @Content),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
			@ApiResponse(responseCode = "404", description = "Locação não encontrada ou Perfil do Cliente não encontrado", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping
	public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest) {
		return ResponseEntity.ok(reservationService.createReservation(reservationRequest));
	}

	@Operation(summary = "Listar Minhas Reservas", description = "Retorna a lista de reservas feitas pelo usuário autenticado, ordenadas por data de início descendente. Requer autenticação.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de reservas retornada"),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping("/my-reservations")
	public ResponseEntity<List<ReservationResponse>> getMyReservations() {
		return ResponseEntity.ok(reservationService.findMyReservations());
	}

	@Operation(summary = "Cancelar Minha Reserva", description = "Cancela uma reserva específica do usuário autenticado. Requer autenticação.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Reserva cancelada com sucesso", content = @Content),
			@ApiResponse(responseCode = "400", description = "Não pode cancelar (ex: status inválido, prazo expirado)", content = @Content),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
			@ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/{id}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelMyReservation(@Parameter @PathVariable UUID id) {
		reservationService.cancelMyReservation(id);
	}


	@Operation(summary = "Confirmar MInha Reserva", description = "Confirma uma serva específica do usuário autenticado. Requer autenticação.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Reserva confirmada com sucesso", content = @Content),
			@ApiResponse(responseCode = "400", description = "Não pode confirmar (ex: status inválido, prazo expirado)", content = @Content),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
			@ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content)
	})
	@PostMapping("/{id}/confirm")
	@SecurityRequirement(name = "bearerAuth")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirmMyReservation(@Parameter @PathVariable UUID id) {
		reservationService.confirmReservation(id);
	}
}
