package com.reservas.api.controller;

import com.reservas.api.dto.LeasesRequest;
import com.reservas.api.dto.LeasesResponse;
import com.reservas.api.service.LeasesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
		return leasesService.listDisponibles(startDate, endDate);
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
}
