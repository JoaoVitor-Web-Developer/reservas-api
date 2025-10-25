package com.reservas.api.repository;

import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.enums.Role;
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
	private LeasesRepository leasesRepository;

	private User user1, user2;
	private Leases lease1, lease2;
	private Reservations res1, res2, res3_cancelled, res4_other_lease;

	@BeforeEach
	void setUpDatabase() {
		// Criar usuários
		user1 = new User(UUID.randomUUID(), "User One", "one@test.com", null, "111", LocalDate.now(), "pass", true, true, true, true, Role.USER);
		user2 = new User(UUID.randomUUID(), "User Two", "two@test.com", null, "222", LocalDate.now(), "pass", true, true, true, true, Role.USER);
		userRepository.saveAll(List.of(user1, user2));

		// Criar Leases
		lease1 = new Leases(UUID.randomUUID(), "Lease A", LeasesType.BEACH_HOUSE, "Desc A", BigDecimal.TEN, 8, 1, LocalDate.now(), null);
		lease2 = new Leases(UUID.randomUUID(), "Lease B", LeasesType.BED, "Desc B", BigDecimal.ONE, 24, 2, LocalDate.now(), null);
		leasesRepository.saveAll(List.of(lease1, lease2));

		// Criar Reservas
		LocalDateTime t1_start = LocalDateTime.of(2025, 11, 10, 10, 0);
		LocalDateTime t1_end = LocalDateTime.of(2025, 11, 10, 12, 0); // lease1 das 10h às 12h

		LocalDateTime t2_start = LocalDateTime.of(2025, 11, 11, 14, 0);
		LocalDateTime t2_end = LocalDateTime.of(2025, 11, 11, 16, 0); // lease1 das 14h às 16h

		LocalDateTime t3_start = LocalDateTime.of(2025, 11, 10, 11, 0);
		LocalDateTime t3_end = LocalDateTime.of(2025, 11, 10, 13, 0); // lease1 das 11h às 13h (CANCELADA)

		LocalDateTime t4_start = LocalDateTime.of(2025, 11, 10, 10, 0);
		LocalDateTime t4_end = LocalDateTime.of(2025, 11, 10, 12, 0); // lease2 das 10h às 12h

		res1 = new Reservations(UUID.randomUUID(), t1_start, t1_end, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now(), ReservationStatus.CONFIRMED, user1, lease1);
		res2 = new Reservations(UUID.randomUUID(), t2_start, t2_end, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now(), ReservationStatus.CONFIRMED, user2, lease1);
		res3_cancelled = new Reservations(UUID.randomUUID(), t3_start, t3_end, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now(), ReservationStatus.CANCELED, user1, lease1);
		res4_other_lease = new Reservations(UUID.randomUUID(), t4_start, t4_end, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now(), ReservationStatus.CANCELED, user2, lease2);

		reservationRepository.saveAll(List.of(res1, res2, res3_cancelled, res4_other_lease));
	}

	@AfterEach
	void tearDown() {
		reservationRepository.deleteAll();
		userRepository.deleteAll();
		leasesRepository.deleteAll();
	}

	// --- Testes para findTiposLocacaoIdComReservasConflitantes ---

	@Test
	@DisplayName("findConflicting... - Deve retornar ID de lease conflitante")
	void findTiposLocacaoIdComReservasConflitantes_shouldReturnConflictingLeaseId() {
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
				.containsExactly(lease1.getId());
	}

	@Test
	@DisplayName("findConflicting... - Deve ignorar reservas canceladas")
	void findTiposLocacaoIdComReservasConflitantes_shouldIgnoreCancelledReservations() {
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
	void findTiposLocacaoIdComReservasConflitantes_shouldReturnEmpty_whenNoConflict() {
		LocalDateTime searchStart = LocalDateTime.of(2025, 11, 10, 13, 0);
		LocalDateTime searchEnd = LocalDateTime.of(2025, 11, 10, 15, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				searchStart,
				searchEnd,
				ReservationStatus.CANCELED
		);

		assertThat(conflictingIds).isNotNull().isEmpty();
	}

	// --- Testes para existsByUser ---
	@Test
	@DisplayName("existsByUser - Deve retornar true se usuário tem reservas")
	void existsByUser_shouldReturnTrue_whenUserHasReservations() {
		boolean exists = reservationRepository.existsByUser(user1);
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("existsByUser - Deve retornar false se usuário não tem reservas")
	void existsByUser_shouldReturnFalse_whenUserHasNoReservations() {
		User user3 = new User(); user3.setId(UUID.randomUUID()); user3.setEmail("three@test.com"); //...
		userRepository.save(user3);

		boolean exists = reservationRepository.existsByUser(user3);
		assertThat(exists).isFalse();
	}


	// --- Testes para existsByLeases ---
	@Test
	@DisplayName("existsByLeases - Deve retornar true se lease tem reservas")
	void existsByLeases_shouldReturnTrue_whenLeaseHasReservations() {
		boolean exists = reservationRepository.existsByLeases(lease1);
		assertThat(exists).isTrue();
	}


	@Test
	@DisplayName("existsByLeases - Deve retornar false se lease não tem reservas")
	void existsByLeases_shouldReturnFalse_whenLeaseHasNoReservations() {
		Leases lease3 = new Leases(); lease3.setId(UUID.randomUUID()); lease3.setName("Lease C");
		leasesRepository.save(lease3);

		boolean exists = reservationRepository.existsByLeases(lease3);
		assertThat(exists).isFalse();
	}
}