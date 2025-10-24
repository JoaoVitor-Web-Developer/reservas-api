package com.reservas.api.repository;

import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservations, UUID> {

	@Query("SELECT r.leases.id FROM Reservations r " +
			"WHERE r.status != 'CANCELED' " +
			"AND (r.startDate < :endDate) " +
			"AND (r.endDate > :startDate)")

	List<UUID> findTypesLocationsIdWithConflictingReservations(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT COUNT(r) > 0 FROM Reservations r " +
			"WHERE r.user = :user " +
			"AND r.status != 'CANCELED' " +
			"AND r.status != 'COMPLETED'")

	boolean existsByUserAndReservationActive(@Param("user") User user);
	boolean existsByLeases(Leases leases);
	List<Reservations> findByUser(User user);
	List<Reservations> findByUserOrderByStartDateDesc(User user);

	boolean existsByUser(User user);

	boolean existsByLeasesAndUser(Leases leases, User user);

	boolean existsByLeasesAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			Leases leases,
			List<ReservationStatus> statusList,
			LocalDateTime newEndDate,   // Mapeia para StartDateLessThanEqual
			LocalDateTime newStartDate  // Mapeia para EndDateGreaterThanEqual
	                                                                                    );}
