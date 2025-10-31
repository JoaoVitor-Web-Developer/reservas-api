package com.reservas.api.service;

import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ForbiddenException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.repository.ClientRepository;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final LeasesRepository leasesRepository;
	private final ClientRepository clientRepository;
	private final ReservationMapper reservationMapper;

	@Transactional
	public ReservationResponse createReservation(ReservationRequest reservationRequest) {
		User currentUser = getCurrentAuthenticatedUser();
		Client currentClient = clientRepository.findByUser(currentUser)
				.orElseThrow(() -> new ResourceNotFoundException("Client not found from user logged."));

		if (reservationRequest.getEndDate().isBefore(reservationRequest.getStartDate()) ||
				reservationRequest.getEndDate().equals(reservationRequest.getStartDate())) {
			throw new BusinessException("End date must be after start date.");
		}


		Leases leases = leasesRepository.findById(reservationRequest.getLeaseId())
				.orElseThrow(() -> new ResourceNotFoundException("Lease not found from user logged."));

		List<UUID> indisponibles = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				reservationRequest.getStartDate(),
				reservationRequest.getEndDate(),
				ReservationStatus.CANCELED
		);

		if (indisponibles.contains(leases.getId())) {
			throw new BusinessException("Lease is not available for the selected dates.");
		}

		long durationHours = ChronoUnit.HOURS.between(reservationRequest.getStartDate(), reservationRequest.getEndDate());

		if (durationHours < leases.getMinTime()) {
			throw new BusinessException("Lease minimum time is " + leases.getMinTime() + " hours.");
		}

		if (durationHours > leases.getMaxTime()) {
			throw new BusinessException("Lease maximum time is " + leases.getMaxTime() + " hours.");
		}

		BigDecimal totalValue = leases.getHourValue().multiply(new BigDecimal(durationHours));

		Reservations newReservation = new Reservations();
		newReservation.setClient(currentClient);
		newReservation.setLeases(leases);
		newReservation.setStartDate(reservationRequest.getStartDate());
		newReservation.setEndDate(reservationRequest.getEndDate());
		newReservation.setTotalValue(totalValue);
		newReservation.setStatus(ReservationStatus.PENDING);

		newReservation.setReservedBy(currentUser);

		Reservations savedReservation = reservationRepository.save(newReservation);
		return reservationMapper.toResponse(savedReservation);
	}


	@Transactional(readOnly = true)
	public List<ReservationResponse> findMyReservations() {
		User currentUser = getCurrentAuthenticatedUser();
		List<Reservations> myReservations = reservationRepository.findByReservedByOrderByStartDateDesc(currentUser);
		return myReservations.stream()
				.map(reservationMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public void cancelMyReservation(UUID id) {
		User currentUser = getCurrentAuthenticatedUser();
		Reservations reservations = reservationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

		if (!reservations.getReservedBy().getId().equals(currentUser.getId())) {
			throw new ForbiddenException("You can only cancel your own reservations.");
		}

		if (reservations.getStatus() == ReservationStatus.COMPLETED || reservations.getStatus() == ReservationStatus.CANCELED) {
			throw new BusinessException("Reservation cannot be canceled.");
		}

		reservations.setStatus(ReservationStatus.CANCELED);
		reservationRepository.save(reservations);
	}

	private User getCurrentAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
			throw new IllegalStateException("User not authenticated correctly.");
		}
		return (User) authentication.getPrincipal();
	}

	@Transactional
	public void confirmReservation(UUID id) {
		User currentUser = getCurrentAuthenticatedUser();
		Reservations reservations = reservationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

		if (!reservations.getReservedBy().getId().equals(currentUser.getId())) {
			throw new ForbiddenException("You can only confirm your own reservations.");
		}

		if (reservations.getStatus() == ReservationStatus.CONFIRMED
				|| reservations.getStatus() == ReservationStatus.CANCELED) {
			throw new BusinessException("Reservation cannot be confirmed.");
		}

		reservations.setStatus(ReservationStatus.CONFIRMED);
		reservationRepository.save(reservations);
	}
}
