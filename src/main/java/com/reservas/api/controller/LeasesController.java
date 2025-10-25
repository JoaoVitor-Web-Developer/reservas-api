package com.reservas.api.controller;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.service.LeasesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leases")
@RequiredArgsConstructor
@Tag(name = "Leases (Locações)", description = "Endpoints para gerenciar e buscar Locações (Leases)") // Agrupa no Swagger UI
public class LeasesController {

	private final LeasesService leasesService;

	@Operation(summary = "Listar tipos de locações disponíveis", description = "Endpoints para gerenciar e buscar Locações (Leases")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de locações disponíveis encontrada"),
			@ApiResponse(responseCode = "400", description = "Datas inválidas (ex: data fim antes da data início)", content = @Content)
	})
	@GetMapping("/disponibles")
	public List<LeasesResponse> getDisponibles(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		LocalDateTime startDateTime = startDate.atStartOfDay(); // Ex: 2025-10-24T00:00:00
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // Ex: 2025-10-24T23:59:59

		return leasesService.listDisponibles(startDateTime, endDateTime);
	}

	@Operation(summary = "Criar Locação", description = "Cria um novo tipo de locação. Requer Role ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Locação criada com sucesso", content = @Content(schema = @Schema(implementation = LeasesResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos na requisição (ex: nome duplicado, tempo min > max)", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é ADMIN)", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LeasesResponse createLease(@RequestBody LeasesRequest leasesRequest) {
		return leasesService.save(leasesRequest);
	}

	@Operation(summary = "Listar Todas as Locações", description = "Retorna uma lista de todos os tipos de locação cadastrados no sistema.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de locações retornada com sucesso")
	})
	@GetMapping
	public List<LeasesResponse> getAll() {
		return leasesService.listAll();
	}

	@Operation(summary = "Buscar Locação por ID", description = "Retorna os detalhes de uma locação específica com base no seu UUID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Locação encontrada e retornada",
			             content = @Content(schema = @Schema(implementation = LeasesResponse.class))), // Example of specifying response schema
			@ApiResponse(responseCode = "404", description = "Locação não encontrada para o ID fornecido",
			             content = @Content) // Empty content for error responses
	})
	@GetMapping("/{id}")
	public LeasesResponse getLease(@PathVariable UUID id) {
		return leasesService.findById(id);
	}

	@Operation(summary = "Atualizar Locação", description = "Atualiza as informações de uma locação existente. Requer Role ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Locação atualizada com sucesso", content = @Content(schema = @Schema(implementation = LeasesResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos na requisição (ex: nome duplicado, tempo min > max)", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é ADMIN)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Locação não encontrada", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PutMapping("/{id}")
	public LeasesResponse updateLease(@PathVariable UUID id, @RequestBody LeasesRequest leasesRequest) {
		return leasesService.update(id, leasesRequest);
	}

	@Operation(summary = "Deletar Locação", description = "Deleta uma locação existente. Requer Role ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Locação deletada com sucesso", content = @Content),
			@ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é ADMIN)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Locação não encontrada", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@DeleteMapping("/{id}")
	public void deleteLease(@PathVariable UUID id) {
		leasesService.delete(id);
	}

	@Operation(summary = "Contratar (Reservar) Locação", description = "Cria uma nova reserva para uma locação e usuário específicos no período informado. Requer Autenticação.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Reserva criada com sucesso", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos (datas, locação indisponível, etc)", content = @Content),
			@ApiResponse(responseCode = "404", description = "Locação ou Usuário não encontrado", content = @Content),
			@ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
	})
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping("/hire-lease/{id}/{userId}")
	public ResponseEntity<ReservationResponse> hireLease(@PathVariable UUID id, @PathVariable UUID userId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		LocalDateTime startDateTime = startDate.atStartOfDay(); // Ex: 2025-10-24T00:00:00
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		ReservationResponse newReservation = leasesService.hireLease(id, userId, startDateTime, endDateTime);

		return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
	}
}
