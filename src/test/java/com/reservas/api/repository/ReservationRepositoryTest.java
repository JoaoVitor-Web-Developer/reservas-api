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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
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
	static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.1.0")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@DynamicPropertySource
	static void configureTestDatabase(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
	}

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private LeasesRepository leasesRepository;

	private User user1, user2;
	private Client client1, client2;
	private Leases lease1, lease2;
	private Reservations res1, res2, res3_cancelled, res4_otherLease;

	@BeforeEach
	void setUp() {
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

		client1 = new Client();
		client1.setName("Client One");
		client1.setEmail("c1@test.com");
		client1.setPhone("111111111");
		client1.setCpf("11111111111");
		client1.setUser(user1);
		client1 = clientRepository.save(client1);

		client2 = new Client();
		client2.setName("Client Two");
		client2.setEmail("c2@test.com");
		client2.setPhone("222222222");
		client2.setCpf("22222222222");
		client2.setUser(user2);
		client2 = clientRepository.save(client2);

		lease1 = new Leases();
		lease1.setName("Leases A");
		lease1.setType(LeasesType.BEACH_HOUSE);
		lease1.setDescription("Desc A");
		lease1.setHourValue(BigDecimal.TEN);
		lease1.setMaxTime(8);
		lease1.setMinTime(1);
		lease1.setOwner(user1);
		lease1 = leasesRepository.save(lease1);

		lease2 = new Leases();
		lease2.setName("Leases B");
		lease2.setType(LeasesType.BEACH_HOUSE);
		lease2.setDescription("Desc B");
		lease2.setHourValue(BigDecimal.ONE);
		lease2.setMaxTime(24);
		lease2.setMinTime(2);
		lease2.setOwner(user2);
		lease2 = leasesRepository.save(lease2);

		LocalDateTime start1 = LocalDateTime.of(2025, 11, 10, 10, 0);
		LocalDateTime end1 = LocalDateTime.of(2025, 11, 10, 12, 0);
		LocalDateTime start2 = LocalDateTime.of(2025, 11, 11, 14, 0);
		LocalDateTime end2 = LocalDateTime.of(2025, 11, 11, 16, 0);

		res1 = new Reservations();
		res1.setClient(client1);
		res1.setLeases(lease1);
		res1.setReservedBy(user1);
		res1.setStartDate(start1);
		res1.setEndDate(end1);
		res1.setTotalValue(BigDecimal.ZERO);
		res1.setStatus(ReservationStatus.CONFIRMED);

		res2 = new Reservations();
		res2.setClient(client2);
		res2.setLeases(lease1);
		res2.setReservedBy(user2);
		res2.setStartDate(start2);
		res2.setEndDate(end2);
		res2.setTotalValue(BigDecimal.ZERO);
		res2.setStatus(ReservationStatus.CONFIRMED);

		res3_cancelled = new Reservations();
		res3_cancelled.setClient(client1);
		res3_cancelled.setLeases(lease1);
		res3_cancelled.setReservedBy(user1);
		res3_cancelled.setStartDate(start1.plusHours(1));
		res3_cancelled.setEndDate(end1.plusHours(1));
		res3_cancelled.setTotalValue(BigDecimal.ZERO);
		res3_cancelled.setStatus(ReservationStatus.CANCELED);

		res4_otherLease = new Reservations();
		res4_otherLease.setClient(client2);
		res4_otherLease.setLeases(lease2);
		res4_otherLease.setReservedBy(user2);
		res4_otherLease.setStartDate(start1);
		res4_otherLease.setEndDate(end1);
		res4_otherLease.setTotalValue(BigDecimal.ZERO);
		res4_otherLease.setStatus(ReservationStatus.CONFIRMED);

		reservationRepository.saveAll(List.of(res1, res2, res3_cancelled, res4_otherLease));
	}

	@AfterEach
	void tearDown() {
		reservationRepository.deleteAll();
		leasesRepository.deleteAll();
		clientRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void findLeasesIdsWithConflictingReservations_shouldReturnConflicts() {
		LocalDateTime searchStart = LocalDateTime.of(2025, 11, 10, 11, 0);
		LocalDateTime searchEnd = LocalDateTime.of(2025, 11, 10, 13, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				searchStart, searchEnd, ReservationStatus.CANCELED);

		assertThat(conflictingIds)
				.isNotNull()
				.contains(lease1.getId());
	}

	@Test
	void findLeasesIdsWithConflictingReservations_shouldIgnoreCancelled() {
		LocalDateTime start = LocalDateTime.of(2025, 11, 10, 12, 0);
		LocalDateTime end = LocalDateTime.of(2025, 11, 10, 13, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				start, end, ReservationStatus.CANCELED);

		assertThat(conflictingIds).isEmpty();
	}

	@Test
	void findLeasesIdsWithConflictingReservations_shouldReturnEmpty() {
		LocalDateTime start = LocalDateTime.of(2025, 11, 10, 13, 0);
		LocalDateTime end = LocalDateTime.of(2025, 11, 10, 15, 0);

		List<UUID> conflictingIds = reservationRepository.findTypesLocationsIdWithConflictingReservations(
				start, end, ReservationStatus.CANCELED);

		assertThat(conflictingIds).isEmpty();
	}

	@Test
	void existsByLeases_shouldReturnTrue() {
		assertThat(reservationRepository.existsByLeases(lease1)).isTrue();
	}

	@Test
	void existsByLeases_shouldReturnFalse() {
		Leases lease3 = new Leases();
		lease3.setName("Leases C");
		lease3.setOwner(user1);
		lease3.setType(LeasesType.BEACH_HOUSE);
		lease3.setHourValue(BigDecimal.ONE);
		lease3.setMinTime(1);
		lease3.setMaxTime(1);
		lease3 = leasesRepository.save(lease3);

		assertThat(reservationRepository.existsByLeases(lease3)).isFalse();
	}

	@Test
	void existsByClient_shouldReturnTrue() {
		assertThat(reservationRepository.existsByClient(client1)).isTrue();
	}

	@Test
	void existsByClient_shouldReturnFalse() {
		User newUser = new User();
		newUser.setEmail("noreserv@test.com");
		newUser.setPassword("p");
		newUser.setRole(Role.USER);
		newUser = userRepository.save(newUser);

		Client client3 = new Client();
		client3.setUser(newUser);
		client3.setName("Client 3");
		client3.setEmail("c3@test.com");
		client3.setCpf("33333333333");
		client3.setPhone("333");
		client3 = clientRepository.save(client3);

		assertThat(reservationRepository.existsByClient(client3)).isFalse();
	}
}
