package com.reservas.api.service;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ForbiddenException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.entities.mapper.LeasesMapper;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
			throw new BusinessException("Min time must be less than max time");
		}

		User owner = getCurrentAuthenticatedUser();

		Leases newLease = leasesMapper.toEntity(leasesRequest);
		newLease.setOwner(owner);

		Leases savedLease = leasesRepository.save(newLease);
		return leasesMapper.toResponse(savedLease);
 	}

	@Transactional
	public LeasesResponse update(UUID id, LeasesRequest leasesRequest) {
		Leases existingLease = leasesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + id));

		checkOwnershipOrAdmin(existingLease.getOwner());

		leasesRepository.findByName(leasesRequest.getName()).ifPresent(lease -> {
			if (!lease.getId().equals(id)) {
				throw new BusinessException("Leases with name " + leasesRequest.getName() + " already exists");
			}
		});

		if (leasesRequest.getMinTime() != null && leasesRequest.getMaxTime() != null &&
				leasesRequest.getMinTime() > leasesRequest.getMaxTime()) {
			throw new BusinessException("Minimum time cannot be greater than maximum time.");
		} else if(leasesRequest.getMinTime() != null && leasesRequest.getMinTime() > existingLease.getMaxTime()) {
			throw new BusinessException("Minimum time cannot be greater than maximum time.");
		} else if (leasesRequest.getMaxTime() != null && existingLease.getMinTime() > leasesRequest.getMaxTime()) {
			throw new BusinessException("Minimum time cannot be greater than maximum time.");
		}

		leasesMapper.updateEntityFromRequest(leasesRequest, existingLease);
		Leases updatedLease = leasesRepository.save(existingLease);
		return leasesMapper.toResponse(updatedLease);
	}

	@Transactional
	public void delete(UUID id) {
		Leases leases = leasesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + id));

		checkOwnershipOrAdmin(leases.getOwner());

		if (reservationRepository.existsByLeases(leases)) {
			throw new BusinessException("Leases cannot be deleted as it has associated reservations");
		}

		leasesRepository.delete(leases);
	}

	@Transactional(readOnly = true)
	public List<LeasesResponse> listDisponibles(LocalDateTime startDate, LocalDateTime endDate) {
		if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
			throw new BusinessException("End date/time must be after start date/time");
		}

		List<UUID> leasesIndisponibles = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				startDate,
				endDate,
				ReservationStatus.CANCELED
		);

		List<Leases> leasesDisponibles;

		if (leasesIndisponibles.isEmpty()) {
			leasesDisponibles = leasesRepository.findAll();
		} else {

			leasesDisponibles = leasesRepository.findByIdNotIn(leasesIndisponibles);
		}

		return leasesDisponibles.stream()
		                        .map(leasesMapper::toResponse)
		                        .collect(Collectors.toList());
	}

	private User getCurrentAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
			throw new IllegalStateException("User not authenticated correctly.");
		}
		return (User) authentication.getPrincipal();
	}

	private void checkOwnershipOrAdmin(User owner) {
		User currentUser = getCurrentAuthenticatedUser();
		boolean isAdmin = currentUser.getAuthorities().stream()
		                             .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin && !currentUser.getId().equals(owner.getId())) {
			throw new ForbiddenException("Access denied. You can only manage your own locacoes.");
		}
	}
}
