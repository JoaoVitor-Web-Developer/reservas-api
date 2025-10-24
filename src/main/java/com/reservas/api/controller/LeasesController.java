package com.reservas.api.controller;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.service.LeasesService;
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
public class LeasesController {

	private final LeasesService leasesService;

	@GetMapping("/disponibles")
	public List<LeasesResponse> getDisponibles(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		LocalDateTime startDateTime = startDate.atStartOfDay(); // Ex: 2025-10-24T00:00:00
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // Ex: 2025-10-24T23:59:59

		return leasesService.listDisponibles(startDateTime, endDateTime);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LeasesResponse createLease(@RequestBody LeasesRequest leasesRequest) {
		return leasesService.save(leasesRequest);
	}

	@GetMapping
	public List<LeasesResponse> getAll() {
		return leasesService.listAll();
	}

	@GetMapping("/{id}")
	public LeasesResponse getLease(@PathVariable UUID id) {
		return leasesService.findById(id);
	}

	@PutMapping("/{id}")
	public LeasesResponse updateLease(@PathVariable UUID id, @RequestBody LeasesRequest leasesRequest) {
		return leasesService.update(id, leasesRequest);
	}

	@DeleteMapping("/{id}")
	public void deleteLease(@PathVariable UUID id) {
		leasesService.delete(id);
	}

	@PostMapping("/hire-lease/{id}/{userId}")
	public ResponseEntity<ReservationResponse> hireLease(@PathVariable UUID id, @PathVariable UUID userId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		LocalDateTime startDateTime = startDate.atStartOfDay(); // Ex: 2025-10-24T00:00:00
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		ReservationResponse newReservation = leasesService.hireLease(id, userId, startDateTime, endDateTime);

		return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
	}
}
