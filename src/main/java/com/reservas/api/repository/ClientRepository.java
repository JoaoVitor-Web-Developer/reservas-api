package com.reservas.api.repository;

import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
	Optional<Client> findByUser(User user);
	Optional<Client> findByCpf(String cpf);
	Optional<Client> findByEmail(String email);
}
