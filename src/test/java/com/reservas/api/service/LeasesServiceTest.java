package com.reservas.api.service;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.mapper.LeasesMapper;
import com.reservas.api.entities.mapper.ReservationMapper;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.repository.LeasesRepository;
import com.reservas.api.repository.ReservationRepository;
import com.reservas.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
		lease.setCreatedAt(LocalDateTime.now());

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
		user.setEmail("test@user.com");
		user.setRole(Role.USER);

		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(user);
		when(authentication.isAuthenticated()).thenReturn(true);
		SecurityContext context = mock(SecurityContext.class);
		when(context.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(context);
	}

	@Test
	void save_shouldSaveLease_whenDataIsValid() {
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.empty());
		when(leasesMapper.toEntity(leasesRequest)).thenReturn(lease);
		when(leasesRepository.save(any(Leases.class))).thenReturn(lease);
		when(leasesMapper.toResponse(any(Leases.class))).thenReturn(leasesResponse);

		LeasesResponse result = leasesService.save(leasesRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(leaseId);
		verify(leasesRepository).save(any(Leases.class));
	}

	@Test
	void save_shouldThrowBusinessException_whenNameExists() {
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.of(lease));

		assertThatThrownBy(() -> leasesService.save(leasesRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("already exists");

		verify(leasesRepository, never()).save(any(Leases.class));
	}

	@Test
	void save_shouldThrowBusinessException_whenMinTimeGreaterThanMaxTime() {
		leasesRequest.setMinTime(10);
		leasesRequest.setMaxTime(5);

		assertThatThrownBy(() -> leasesService.save(leasesRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("minimum time cannot be greater");

		verify(leasesRepository, never()).save(any(Leases.class));
	}

	@Test
	void update_shouldUpdateLease_whenDataIsValid() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(leasesRepository.findByName(leasesRequest.getName())).thenReturn(Optional.empty());
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
	void update_shouldThrowResourceNotFoundException_whenLeaseNotFound() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> leasesService.update(leaseId, leasesRequest))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void delete_shouldDeleteLease_whenNoReservationsExist() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(reservationRepository.existsByLeases(lease)).thenReturn(false);
		doNothing().when(leasesRepository).delete(lease);

		assertThatCode(() -> leasesService.delete(leaseId)).doesNotThrowAnyException();

		verify(leasesRepository).delete(lease);
	}

	@Test
	void delete_shouldThrowBusinessException_whenReservationsExist() {
		when(leasesRepository.findById(leaseId)).thenReturn(Optional.of(lease));
		when(reservationRepository.existsByLeases(lease)).thenReturn(true);

		assertThatThrownBy(() -> leasesService.delete(leaseId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("cannot be deleted as it has associated reservations");

		verify(leasesRepository, never()).delete(any(Leases.class));
	}

	@Test
	void listDisponibles_shouldThrowBusinessException_whenEndDateIsBeforeStartDate() {
		LocalDateTime invalidEndDateTime = testStartDateTime.minusDays(1);

		assertThatThrownBy(() -> leasesService.listDisponibles(testStartDateTime, invalidEndDateTime))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("End date/time must be after start date/time");
	}
}
