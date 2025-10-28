package com.reservas.api.repository;

import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClientRepositoryTest {

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void whenFindByUser_thenReturnClient() {
		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("pass");
		user = userRepository.save(user);

		Client client = new Client();
		client.setName("Joao Test");
		client.setCpf("00011122233");
		client.setEmail("client@example.com");
		client.setPhone("11999999999");
		client.setCreatedAt(LocalDateTime.now());
		client.setUser(user);

		Client saved = clientRepository.save(client);

		Optional<Client> found = clientRepository.findByUser(user);
		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(saved.getId());

		// also test findByCpf
		Optional<Client> byCpf = clientRepository.findByCpf("00011122233");
		assertThat(byCpf).isPresent();
		assertThat(byCpf.get().getCpf()).isEqualTo("00011122233");
	}
}
