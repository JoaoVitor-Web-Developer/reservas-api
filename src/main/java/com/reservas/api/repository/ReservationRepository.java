package com.reservas.api.repository;

import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Adicione @Repository

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservations, UUID> {

	@Query("SELECT r.leases.id FROM Reservations r " +
			"WHERE r.status != :statusCancelada " +
			"AND (r.startDate < :endDate) " +
			"AND (r.endDate > :startDate)"
	)

	List<UUID> findTypesLocationsIdWithConflictingReservations(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			@Param("statusCancelada") ReservationStatus statusCancelada
	);

	@Query("SELECT COUNT(r) > 0 FROM Reservations r " +
			"WHERE r.user = :user " +
			"AND r.status NOT IN (:excludedStatuses)")
	boolean existsByUserAndActiveReservations(
			@Param("user") User user,
			@Param("excludedStatuses") List<ReservationStatus> excludedStatuses
	);


	boolean existsByLeases(Leases leases);

	List<Reservations> findByUser(User user);

	boolean existsByUser(User user);


	boolean existsByLeasesAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			Leases leases,
			List<ReservationStatus> statusList,
			LocalDateTime endDateQueryParam, // O parâmetro 3 é comparado com startDate (<= endDate)
			LocalDateTime startDateQueryParam // O parâmetro 4 é comparado com endDate (>= startDate)
	);
}