package com.reservas.api.service;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.mapper.LeasesMapper;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeasesServiceTest {

	@Mock
	private LeasesRepository leasesRepository;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private LeasesMapper leasesMapper;
	@Mock
	private ReservationMapper reservationMapper;

	@InjectMocks
	private LeasesService leasesService;

	private UUID leaseId;
	private UUID userId;
	private LeasesRequest leasesRequest;
	private Leases lease;
	private LeasesResponse leasesResponse;
	private User user;
	private Reservations reservation;
	private ReservationResponse reservationResponse;
	private LocalDateTime testStartDateTime;
	private LocalDateTime testEndDateTime;

	@BeforeEach
	void setUp() {
		leaseId = UUID.randomUUID();
		userId = UUID.randomUUID();
		testStartDateTime = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		testEndDateTime = LocalDateTime.of(2025, 11, 12, 18, 0, 0);

		leasesRequest = new LeasesRequest();
		leasesRequest.setName("Sala de Reunião");
		leasesRequest.setType(LeasesType.CORPORATE);
		leasesRequest.setDescription("Sala equipada para 10 pessoas");
		leasesRequest.setHourValue(new BigDecimal("50.00"));
		leasesRequest.setMaxTime(8);
		leasesRequest.setMinTime(1);

		lease = new Leases();
		lease.setId(leaseId);
		lease.setName("Sala de Reunião");
		lease.setType(LeasesType.CORPORATE);
		lease.setDescription("Sala equipada para 10 pessoas");
		lease.setHourValue(new BigDecimal("50.00"));
		lease.setMaxTime(8);
		lease.setMinTime(1);
		lease.setCreatedAt(LocalDate.now());

		leasesResponse = new LeasesResponse();
		leasesResponse.setId(leaseId);
		leasesResponse.setName("Sala de Reunião");
		leasesResponse.setType(LeasesType.CORPORATE);
		leasesResponse.setDescription("Sala equipada para 10 pessoas");
		leasesResponse.setHourValue(new BigDecimal("50.00"));
		leasesResponse.setMaxTime(8);
		leasesResponse.setMinTime(1);
		leasesResponse.setCreatedAt(LocalDate.now());

		user = new User();
		user.setId(userId);
		user.setName("Test User");
		user.setEmail("test@user.com");
		user.setRole(Role.USER);
		user.setCreatedAt(LocalDate.now());

		reservation = new Reservations();
		reservation.setId(UUID.randomUUID());
		reservation.setUser(user);
		reservation.setLeases(lease);
		reservation.setStartDate(testStartDateTime);
		reservation.setEndDate(testEndDateTime);
		reservation.setStatus(ReservationStatus.CONFIRMED);
		reservation.setTotalValue(new BigDecimal("300.00"));
		reservation.setCreatedAt(LocalDateTime.now());
		reservation.setUpdatedAt(LocalDateTime.now());

		reservationResponse = new ReservationResponse();
		reservationResponse.setId(reservation.getId());
		reservationResponse.setUserId(user.getId());
		reservationResponse.setLeaseId(lease.getId());
		reservationResponse.setStartDate(testStartDateTime);
		reservationResponse.setEndDate(testEndDateTime);
		reservationResponse.setStatus(ReservationStatus.CONFIRMED);
		reservationResponse.setTotalValue(new BigDecimal("300.00"));
		reservationResponse.setCreatedAt(LocalDateTime.now());
	}

	// --- Testes para save ---
	@Test
	@DisplayName("save - Deve salvar locação com sucesso")
	void save_shouldSaveLease_whenDataIsValid() {
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.empty());
		when(leasesMapper.toEntity(leasesRequest)).thenReturn(lease);
		when(leasesRepository.save(any(Leases.class))).thenAnswer(invocation -> {
			Leases saved = invocation.getArgument(0);
			saved.setId(leaseId);
			saved.setCreatedAt(LocalDate.now());
			return saved;
		});
		when(leasesMapper.toResponse(any(Leases.class))).thenReturn(leasesResponse);

		LeasesResponse result = leasesService.save(leasesRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(leaseId);
		verify(leasesRepository).save(any(Leases.class));
	}

	@Test
	@DisplayName("save - Deve lançar BusinessException se nome já existe")
	void save_shouldThrowBusinessException_whenNameExists() {
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.of(lease));

		assertThatThrownBy(() -> leasesService.save(leasesRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("already exists");
		verify(leasesRepository, never()).save(any(Leases.class));
	}

	@Test
	@DisplayName("save - Deve lançar BusinessException se minTime > maxTime")
	void save_shouldThrowBusinessException_whenMinTimeGreaterThanMaxTime() {
		leasesRequest.setMinTime(10);
		leasesRequest.setMaxTime(5);

		assertThatThrownBy(() -> leasesService.save(leasesRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("minimum time cannot be greater than maximum time");
		verify(leasesRepository, never()).save(any(Leases.class));
	}


	// --- Testes para update ---
	@Test
	@DisplayName("update - Deve atualizar locação com sucesso")
	void update_shouldUpdateLease_whenDataIsValid() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.of(lease));

		doNothing().when(leasesMapper).updateEntityFromRequest(leasesRequest, lease);
		when(leasesRepository.save(lease)).thenReturn(lease);
		when(leasesMapper.toResponse(lease)).thenReturn(leasesResponse);

		LeasesResponse result = leasesService.update(leaseId, leasesRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(leaseId);
		verify(leasesMapper).updateEntityFromRequest(leasesRequest, lease);
		verify(leasesRepository).save(lease);
	}

	@Test
	@DisplayName("update - Deve lançar ResourceNotFoundException se locação não existe")
	void update_shouldThrowResourceNotFoundException_whenLeaseNotFound() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> leasesService.update(leaseId, leasesRequest))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(leasesRepository, never()).save(any(Leases.class));
	}

	// --- Testes para delete ---
	@Test
	@DisplayName("delete - Deve deletar locação com sucesso se não houver reservas")
	void delete_shouldDeleteLease_whenNoReservationsExist() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(reservationRepository.existsByLeases(lease)).thenReturn(false);
		doNothing().when(leasesRepository).delete(lease);

		assertThatCode(() -> leasesService.delete(leaseId)).doesNotThrowAnyException();

		verify(leasesRepository).delete(lease);
	}


	@Test
	@DisplayName("delete - Deve lançar BusinessException se locação tiver reservas")
	void delete_shouldThrowBusinessException_whenReservationsExist() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(reservationRepository.existsByLeases(lease)).thenReturn(true);

		assertThatThrownBy(() -> leasesService.delete(leaseId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("cannot be deleted as it has associated reservations");
		verify(leasesRepository, never()).delete(any(Leases.class));
	}

	// --- Testes para listDisponibles ---

	@Test
	@DisplayName("listDisponibles - Deve lançar BusinessException se data fim for antes de data início")
	void listDisponibles_shouldThrowBusinessException_whenEndDateIsBeforeStartDate() {
		LocalDateTime invalidEndDateTime = testStartDateTime.minusDays(1);

		assertThatThrownBy(() -> leasesService.listDisponibles(testStartDateTime, invalidEndDateTime))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("End date/time must be after start date/time");
	}

	@Test
	@DisplayName("hireLease - Deve criar reserva com sucesso")
	void hireLease_shouldCreateReservation_whenDataIsValidAndAvailable() {
		// Ajusta as datas para respeitar minTime/maxTime da 'lease' (1 a 8 horas)
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 14, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Simula que NÃO há conflitos para este período específico
		when(reservationRepository.findTypesLocationsIdWithConflictingReservations(hireStart, hireEnd, ReservationStatus.CANCELED))
				.thenReturn(Collections.emptyList());
		when(reservationRepository.save(any(Reservations.class))).thenAnswer(invocation -> {
			Reservations r = invocation.getArgument(0);
			r.setId(UUID.randomUUID());
			r.setStatus(ReservationStatus.CONFIRMED);
			long hours = java.time.temporal.ChronoUnit.HOURS.between(hireStart, hireEnd);
			r.setTotalValue(lease.getHourValue().multiply(new BigDecimal(hours)));
			return r;
		});
		when(reservationMapper.toResponse(any(Reservations.class))).thenReturn(reservationResponse);


		ReservationResponse result = leasesService.hireLease(leaseId, userId, hireStart, hireEnd);


		assertThat(result).isNotNull();
		verify(reservationRepository).save(any(Reservations.class));
	}


	@Test
	@DisplayName("hireLease - Deve lançar BusinessException se locação não disponível")
	void hireLease_shouldThrowBusinessException_whenLeaseNotAvailable() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 14, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Simula que HÁ conflito
		when(reservationRepository.findTypesLocationsIdWithConflictingReservations(hireStart, hireEnd, ReservationStatus.CANCELED))
				.thenReturn(List.of(leaseId));

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Lease is not available");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}

	@Test
	@DisplayName("hireLease - Deve lançar BusinessException se exceder maxTime")
	void hireLease_shouldThrowBusinessException_whenDurationExceedsMaxTime() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 19, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Assumindo que está disponível para testar a validação de tempo
		when(reservationRepository.findTypesLocationsIdWithConflictingReservations(hireStart, hireEnd, ReservationStatus.CANCELED)).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("exceeds the maximum allowed time");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}


	@Test
	@DisplayName("hireLease - Deve lançar BusinessException se abaixo de minTime")
	void hireLease_shouldThrowBusinessException_whenDurationBelowMinTime() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 10, 30, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Assumindo que está disponível para testar a validação de tempo
		when(reservationRepository.findTypesLocationsIdWithConflictingReservations(hireStart, hireEnd, ReservationStatus.CANCELED)).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("is less than the minimum required time");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}

	@Test
	@DisplayName("hireLease - Deve lançar BusinessException se data fim for igual ou antes da data início")
	void hireLease_shouldThrowBusinessException_whenEndDateNotAfterStartDate() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 10, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("End date/time must be after start date/time");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}


	@Test
	@DisplayName("hireLease - Deve lançar ResourceNotFoundException se Lease não existe")
	void hireLease_shouldThrowResourceNotFoundException_whenLeaseNotFound() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 14, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Lease not found");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}

	@Test
	@DisplayName("hireLease - Deve lançar ResourceNotFoundException se User não existe")
	void hireLease_shouldThrowResourceNotFoundException_whenUserNotFound() {
		LocalDateTime hireStart = LocalDateTime.of(2025, 11, 10, 10, 0, 0);
		LocalDateTime hireEnd = LocalDateTime.of(2025, 11, 10, 14, 0, 0);

		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> leasesService.hireLease(leaseId, userId, hireStart, hireEnd))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("User not found");
		verify(reservationRepository, never()).save(any(Reservations.class));
	}
}