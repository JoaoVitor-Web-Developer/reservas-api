package com.reservas.api.repository;

import com.reservas.api.entities.model.Leases;
import com.reservas.api.entities.model.LeasesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeasesRepository extends JpaRepository<Leases, UUID> {
	Optional<Leases> findByName(String name);
	Optional<Leases> findByType(LeasesType typeLease);
	List<Leases> findAllByIdNotIn(List<UUID> ids);
}
