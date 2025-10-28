package com.reservas.api.repository;

import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservations, UUID> {

	List<Reservations> findByReservedBy(User reservedBy);

	@Query("SELECT r.leases.id FROM Reservations r " +
			"WHERE r.status != :excludedStatus " +
			"AND (r.startDate < :endDate) " +
			"AND (r.endDate > :startDate)"
	)
	List<UUID> findTypesLocationsIdWithConflictingReservations(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			@Param("excludedStatus") ReservationStatus excludedStatus
	);

	@Query("SELECT COUNT(r) > 0 FROM Reservations r " +
			"WHERE r.reservedBy = :user " +
			"AND r.status NOT IN (:excludedStatuses)")
	boolean existsByUserAndActiveReservations(
			@Param("user") User user,
			@Param("excludedStatuses") List<ReservationStatus> excludedStatuses
	);


	boolean existsByLeases(Leases leases);

	List<Reservations> findByReservedByOrderByStartDateDesc(User reservadoPor);

	boolean existsByClient(Client client);


	boolean existsByLeasesAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			Leases leases,
			List<ReservationStatus> statusList,
			LocalDateTime endDateQueryParam,
			LocalDateTime startDateQueryParam
	);
}