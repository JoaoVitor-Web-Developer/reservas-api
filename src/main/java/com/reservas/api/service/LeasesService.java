package com.reservas.api.service;

import com.reservas.api.dto.LeasesRequest;
import com.reservas.api.dto.LeasesResponse;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.mapper.LeasesMapper;
import com.reservas.api.model.Leases;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeasesService {

	private final LeasesRepository leasesRepository;
	private final LeasesMapper leasesMapper;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public List<LeasesResponse> listAll() {
		return leasesRepository.findAll().stream()
				.map(leasesMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public LeasesResponse findById(UUID id) {
		Leases leases = leasesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + id));
		return leasesMapper.toResponse(leases);
	}


	@Transactional
	public LeasesResponse save(LeasesRequest leasesRequest) {
		leasesRepository.findByName(leasesRequest.getName()).ifPresent(lease -> {
			throw new BusinessException("Leases with name " + leasesRequest.getName() + " already exists");
		});

		if (leasesRequest.getMinTime() > leasesRequest.getMaxTime()) {
			throw new BusinessException("Leases minimum time is " + leasesRequest.getMinTime());
		}

		Leases newLeases = leasesMapper.toEntity(leasesRequest);
		newLeases.setCreatedAt(LocalDate.now());

		Leases savedLeases = leasesRepository.save(leasesMapper.toEntity(leasesRequest));
		return leasesMapper.toResponse(savedLeases);
	}

	@Transactional
	public LeasesResponse update(UUID id, LeasesRequest leasesRequest) {
		 Leases existingLeases = leasesRepository.findById(id)
				 .orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + id));

		 leasesRepository.findByName(leasesRequest.getName()).ifPresent(lease -> {
			 if (!lease.getId().equals(id)) {
				 throw new BusinessException("Leases with name " + leasesRequest.getName() + " already exists");
			 }
		 });

		 if (leasesRequest.getMinTime() > leasesRequest.getMaxTime()) {
			 throw new BusinessException("Min time must be less than max time");
		 }

		 leasesMapper.updateEntityFromRequest(leasesRequest, existingLeases);
		 Leases leasesUpdated = leasesRepository.save(existingLeases);
		 return leasesMapper.toResponse(leasesUpdated);
	}

	@Transactional
	public void delete(UUID id) {
		Leases leases = leasesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + id));

		if (reservationRepository.existsByLeases(leases)) {
			throw new BusinessException("Cannot be deleted. This rental type already has associated reservations");
		}

		leasesRepository.delete(leases);
	}

	@Transactional(readOnly = true)
	public List<LeasesResponse> listDisponibles(LocalDateTime startDate, LocalDateTime endDate) {
		if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
			throw new BusinessException("Start date must be after end date");
		}

		List<UUID> leasesIndisponibles = reservationRepository.findTypesLocationsIdWithConflictingReservations(startDate, endDate);
		List<Leases> leasesDisponibles;

		if (leasesIndisponibles.isEmpty()) {
			leasesDisponibles = leasesRepository.findAll();
		} else {
			leasesDisponibles = leasesRepository.findAllByIdNotIn(leasesIndisponibles);
		}

		return leasesDisponibles.stream()
				.map(leasesMapper::toResponse)
				.collect(Collectors.toList());
	}
}
