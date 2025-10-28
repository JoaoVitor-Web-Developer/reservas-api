package com.reservas.api.repository;

import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeasesRepository extends JpaRepository<Leases, UUID> {
	Optional<Leases> findByName(String name);
	List<Leases> findByIdNotIn(Collection<UUID> ids);
	List<Leases> findByOwner(User owner);
	boolean existsByIdAndOwner(UUID id, User owner);
}
