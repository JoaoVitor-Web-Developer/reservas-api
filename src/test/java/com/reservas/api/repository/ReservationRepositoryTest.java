package com.reservas.api.repository;

import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ReservationRepositoryTest {

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.1.0")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ClientRepository clienteRepository;
	@Autowired
	private LeasesRepository locacaoRepository;

	private User user1, user2;
	private Client cliente1, cliente2;
	private Leases locacao1, locacao2;
	private Reservations res1, res2, res3_cancelled, res4_other_locacao;

	@BeforeEach
	void setUpDatabase() {
		user1 = new User();
		user1.setEmail("one@test.com");
		user1.setPassword("hashedPass1");
		user1.setRole(Role.USER);
		user1 = userRepository.save(user1);

		user2 = new User();
		user2.setEmail("two@test.com");
		user2.setPassword("hashedPass2");
		user2.setRole(Role.USER);
		user2 = userRepository.save(user2);

		cliente1 = new Client();
		cliente1.setName("User One Profile");
		cliente1.setEmail("one-profile@test.com");
		cliente1.setPhone("111111");
		cliente1.setCpf("111.111.111-11");
		cliente1.setUser(user1);
		cliente1 = clienteRepository.save(cliente1);
		cliente2 = new Client();
		cliente2.setName("User Two Profile");
		cliente2.setEmail("two-profile@test.com");
		cliente2.setPhone("222222");
		cliente2.setCpf("222.222.222-22");
		cliente2.setUser(user2);
		cliente2 = clienteRepository.save(cliente2);

		locacao1 = new Leases();
		locacao1.setName("Leases A");
		locacao1.setType(LeasesType.BEACH_HOUSE);
		locacao1.setDescription("Desc A");
		locacao1.setHourValue(BigDecimal.TEN);
		locacao1.setMaxTime(8);
		locacao1.setMinTime(1);
		locacao1.setOwner(user1);
		locacao1 = locacaoRepository.save(locacao1);

		locacao2 = new Leases();
		locacao2.setName("Leases B");
		locacao2.setType(LeasesType.BEACH_HOUSE);
		locacao2.setDescription("Desc B");
		locacao2.setHourValue(BigDecimal.ONE);
		locacao2.setMaxTime(24);
		locacao2.setMinTime(2);
		locacao2.setOwner(user2);
		locacao2 = locacaoRepository.save(locacao2);

		LocalDateTime t1_start = LocalDateTime.of(2025, 11, 10, 10, 0);
		LocalDateTime t1_end = LocalDateTime.of(2025, 11, 10, 12, 0);

		LocalDateTime t2_start = LocalDateTime.of(2025, 11, 11, 14, 0);
		LocalDateTime t2_end = LocalDateTime.of(2025, 11, 11, 16, 0);

		LocalDateTime t3_start = LocalDateTime.of(2025, 11, 10, 11, 0);
		LocalDateTime t3_end = LocalDateTime.of(2025, 11, 10, 13, 0);

		LocalDateTime t4_start = LocalDateTime.of(2025, 11, 10, 10, 0);
		LocalDateTime t4_end = LocalDateTime.of(2025, 11, 10, 12, 0);

		res1 = new Reservations();
		res1.setClient(cliente1);
		res1.setLeases(locacao1);
		res1.setReservedBy(user1);
		res1.setStartDate(t1_start);
		res1.setEndDate(t1_end);
		res1.setTotalValue(BigDecimal.ZERO);
		res1.setStatus(ReservationStatus.CONFIRMED);

		res2 = new Reservations();
		res2.setClient(cliente2);
		res2.setLeases(locacao1);
		res2.setReservedBy(user2);
		res2.setStartDate(t2_start);
		res2.setEndDate(t2_end);
		res2.setTotalValue(BigDecimal.ZERO);
		res2.setStatus(ReservationStatus.CONFIRMED);

		res3_cancelled = new Reservations();
		res3_cancelled.setClient(cliente1);
		res3_cancelled.setLeases(locacao1);
		res3_cancelled.setReservedBy(user1);
		res3_cancelled.setStartDate(t3_start);
		res3_cancelled.setEndDate(t3_end);
		res3_cancelled.setTotalValue(BigDecimal.ZERO);
		res3_cancelled.setStatus(ReservationStatus.CANCELED);

		res4_other_locacao = new Reservations();
		res4_other_locacao.setClient(cliente2);
		res4_other_locacao.setLeases(locacao2);
		res4_other_locacao.setReservedBy(user2);
		res4_other_locacao.setStartDate(t4_start);
		res4_other_locacao.setEndDate(t4_end);
		res4_other_locacao.setTotalValue(BigDecimal.ZERO);
		res4_other_locacao.setStatus(ReservationStatus.CONFIRMED);

		reservationRepository.saveAll(List.of(res1, res2, res3_cancelled, res4_other_locacao));
	}

	@AfterEach
	void tearDown() {
		reservationRepository.deleteAll();
		locacaoRepository.deleteAll();
		clienteRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("findConflicting... - Deve retornar ID (Long) de locacao conflitante")
	void findTypesLocationsIdWithConflictingReservations_shouldReturnConflictingLeaseId() {
		LocalDateTime searchStart = LocalDateTime.of(2025, 11, 10, 11, 0);
		LocalDateTime searchEnd = LocalDateTime.of(2025, 11, 10, 13, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
		                                                                                                   searchStart,
		                                                                                                   searchEnd,
		                                                                                                   ReservationStatus.CANCELED
		                                                                                                 );

		assertThat(conflictingIds)
				.isNotNull()
				.hasSize(1)
				.containsExactly(locacao1.getId());
	}

	@Test
	@DisplayName("findConflicting... - Deve ignorar reservas canceladas")
	void findTypesLocationsIdWithConflictingReservations_shouldIgnoreCancelledReservations() {
		LocalDateTime searchStart = LocalDateTime.of(2025, 11, 10, 12, 0);
		LocalDateTime searchEnd = LocalDateTime.of(2025, 11, 10, 13, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				searchStart,
				searchEnd,
				ReservationStatus.CANCELED
		                                                                                                 );

		assertThat(conflictingIds).isNotNull().isEmpty();
	}


	@Test
	@DisplayName("findConflicting... - Deve retornar vazio se não houver conflito")
	void findTypesLocationsIdWithConflictingReservations_shouldReturnEmpty_whenNoConflict() {
		LocalDateTime searchStart = LocalDateTime.of(2025, 11, 10, 13, 0);
		LocalDateTime searchEnd = LocalDateTime.of(2025, 11, 10, 15, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				searchStart,
				searchEnd,
				ReservationStatus.CANCELED
		                                                                                                 );

		assertThat(conflictingIds).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("existsByUserAndActive... - Deve retornar true se user tem reservas ativas")
	void existsByUserAndActiveReservations_shouldReturnTrue_whenUserHasActiveReservations() {
		List<ReservationStatus> excluded = List.of(ReservationStatus.CANCELED, ReservationStatus.COMPLETED);
		boolean exists = reservationRepository.existsByUserAndActiveReservations(user1, excluded);
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("existsByUserAndActive... - Deve retornar false se user só tem reservas canceladas/completas")
	void existsByUserAndActiveReservations_shouldReturnFalse_whenUserOnlyHasInactiveReservations() {
		res1.setStatus(ReservationStatus.COMPLETED);
		reservationRepository.save(res1);

		List<ReservationStatus> excluded = List.of(ReservationStatus.CANCELED, ReservationStatus.COMPLETED);
		boolean exists = reservationRepository.existsByUserAndActiveReservations(user1, excluded);
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("existsByUserAndActive... - Deve retornar false se user não tem reservas")
	void existsByUserAndActiveReservations_shouldReturnFalse_whenUserHasNoReservations() {
		User user3 = new User(); user3.setEmail("three@test.com"); user3.setPassword("p"); user3.setRole(Role.USER);
		user3 = userRepository.save(user3);

		List<ReservationStatus> excluded = List.of(ReservationStatus.CANCELED, ReservationStatus.COMPLETED);
		boolean exists = reservationRepository.existsByUserAndActiveReservations(user3, excluded);
		assertThat(exists).isFalse();
	}


	@Test
	@DisplayName("existsByLeases - Deve retornar true se locacao tem reservas")
	void existsByLeases_shouldReturnTrue_whenLeaseHasReservations() {
		boolean exists = reservationRepository.existsByLeases(locacao1);
		assertThat(exists).isTrue();
	}


	@Test
	@DisplayName("existsByLeases - Deve retornar false se locacao não tem reservas")
	void existsByLeases_shouldReturnFalse_whenLeaseHasNoReservations() {
		Leases locacao3 = new Leases(); locacao3.setName("Leases C"); locacao3.setOwner(user1);
		locacao3.setType(LeasesType.BEACH_HOUSE); locacao3.setHourValue(BigDecimal.ONE);
		locacao3.setMinTime(1); locacao3.setMaxTime(1);
		locacao3 = locacaoRepository.save(locacao3);

		boolean exists = reservationRepository.existsByLeases(locacao3);
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("existsByClient - Deve retornar true se cliente tem reservas")
	void existsByClient_shouldReturnTrue_whenClientHasReservations() {
		boolean exists = reservationRepository.existsByClient(cliente1);
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("existsByClient - Deve retornar false se cliente não tem reservas")
	void existsByClient_shouldReturnFalse_whenClientHasNoReservations() {
		Client cliente3 = new Client(); cliente3.setUser(user1);
		cliente3.setName("Client 3"); cliente3.setEmail("c3@p.com"); cliente3.setCpf("333"); cliente3.setPhone("3");
		cliente3 = clienteRepository.save(cliente3);

		boolean exists = reservationRepository.existsByClient(cliente3);
		assertThat(exists).isFalse();
	}
}