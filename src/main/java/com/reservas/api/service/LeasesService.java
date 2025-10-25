package com.reservas.api.service;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.entities.mapper.LeasesMapper;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
	private final UserRepository userRepository;
	private final ReservationMapper reservationMapper;

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

		Leases savedLeases = leasesRepository.save(newLeases);
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

	@Transactional
	public ReservationResponse hireLease(UUID leaseId, UUID userId, LocalDateTime startDate, LocalDateTime endDate) {

		if (startDate == null || endDate == null) {
			throw  new BusinessException("Start date and end date cannot be null");
		}

		if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
			throw new BusinessException("End date must be after start date");
		}

		if (startDate.toLocalDate().isBefore(LocalDate.now())) {
			throw new BusinessException("Start date cannot be in the past");
		}

		Leases leases = leasesRepository.findById(leaseId)
				.orElseThrow(() -> new ResourceNotFoundException("Leases type not found with id: " + leaseId));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		boolean hasConflictingReservation = reservationRepository
				.existsByLeasesAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
						leases,
						List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED),
						endDate,
						startDate
		);

		if (hasConflictingReservation) {
			throw  new BusinessException("This property is already reserved for the selected period");
		}

		long numberOfDays = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
		BigDecimal dailyPrice = leases.getHourValue();
		BigDecimal totalValue = dailyPrice.multiply(BigDecimal.valueOf(numberOfDays));

		Reservations reservation = new Reservations();
		reservation.setLeases(leases);
		reservation.setUser(user);
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		reservation.setStatus(ReservationStatus.PENDING);
		reservation.setCreatedAt(LocalDateTime.now());
		reservation.setUpdatedAt(LocalDateTime.now());
		reservation.setTotalValue(totalValue);

		Reservations savedReservation = reservationRepository.save(reservation);

		return reservationMapper.toResponse(savedReservation);
	}
}
