package com.reservas.api.service;

import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.repository.ClientRepository;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private LeasesRepository leasesRepository;

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private ReservationMapper reservationMapper;

	@InjectMocks
	private ReservationService reservationService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void mockAuthenticatedUser(User user) {
		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(user);
		SecurityContext sc = mock(SecurityContext.class);
		when(sc.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(sc);
	}

	@Test
	void createReservation_happyPath_returnsResponse() {
		User user = new User();
		user.setId(UUID.randomUUID());
		mockAuthenticatedUser(user);

		Client client = new Client();
		client.setId(UUID.randomUUID());
		client.setUser(user);

		when(clientRepository.findByUser(user)).thenReturn(Optional.of(client));

		Leases leases = new Leases();
		leases.setId(UUID.randomUUID());
		leases.setHourValue(new BigDecimal("10.00"));
		leases.setMinTime(1);
		leases.setMaxTime(24);

		ReservationRequest req = new ReservationRequest();
		req.setLeaseId(leases.getId());
		req.setStartDate(LocalDateTime.now().plusDays(1));
		req.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));

		when(leasesRepository.findById(leases.getId())).thenReturn(Optional.of(leases));
		when(reservationRepository.existsByLeasesAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
				any(), anyList(), any(), any())).thenReturn(false);

		Reservations saved = new Reservations();
		saved.setId(UUID.randomUUID());
		saved.setClient(client);
		saved.setLeases(leases);
		saved.setStatus(ReservationStatus.PENDING);

		when(reservationRepository.save(any(Reservations.class))).thenReturn(saved);
		ReservationResponse resp = new ReservationResponse();
		resp.setId(saved.getId());
		when(reservationMapper.toResponse(saved)).thenReturn(resp);

		ReservationResponse result = reservationService.createReservation(req);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(saved.getId());
	}

	@Test
	void createReservation_whenLeaseNotFound_throws() {
		User user = new User();
		user.setId(UUID.randomUUID());
		mockAuthenticatedUser(user);

		Client client = new Client();
		client.setId(UUID.randomUUID());
		client.setUser(user);
		when(clientRepository.findByUser(user)).thenReturn(Optional.of(client));

		ReservationRequest req = new ReservationRequest();
		req.setLeaseId(UUID.randomUUID());
		req.setStartDate(LocalDateTime.now().plusDays(1));
		req.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));

		when(leasesRepository.findById(req.getLeaseId())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reservationService.createReservation(req))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void cancelReservation_asOwner_changesStatus() {
		User user = new User();
		user.setId(UUID.randomUUID());
		mockAuthenticatedUser(user);

		Reservations reservation = new Reservations();
		reservation.setId(UUID.randomUUID());
		reservation.setReservedBy(user);
		reservation.setStatus(ReservationStatus.PENDING);

		when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

		reservationService.cancelMyReservation(reservation.getId());

		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
		verify(reservationRepository).save(reservation);
	}
}
