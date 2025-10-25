package com.reservas.api.repository;

import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	private User user1;

	@BeforeEach
	void setUp() {
		user1 = new User();
		user1.setName("User One");
		user1.setEmail("one@email.com");
		user1.setPassword("pass");
		user1.setCpf("11111111111");
		user1.setCreatedAt(LocalDate.now());
		user1.setRole(Role.USER);
		user1.setEnabled(true);
		user1.setAccountNonLocked(true);
		user1.setAccountNonExpired(true);
		user1.setCredentialsNonExpired(true);

		entityManager.persistAndFlush(user1);
	}

	@AfterEach
	void tearDown() {
		entityManager.clear();
	}


	@Test
	@DisplayName("Deve encontrar usuário pelo email quando email existe")
	void findByEmail_shouldReturnUser_whenEmailExists() {
		Optional<User> foundUserOpt = userRepository.findByEmail("one@email.com");

		assertThat(foundUserOpt).isPresent();
		assertThat(foundUserOpt.get().getId()).isEqualTo(user1.getId());
		assertThat(foundUserOpt.get().getName()).isEqualTo("User One");
	}

	@Test
	@DisplayName("Deve retornar vazio ao buscar por email inexistente")
	void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
		Optional<User> foundUserOpt = userRepository.findByEmail("nonexistent@email.com");

		assertThat(foundUserOpt).isNotPresent();
	}

	@Test
	@DisplayName("Deve encontrar usuário pelo CPF quando CPF existe")
	void findByCpf_shouldReturnUser_whenCpfExists() {
		Optional<User> foundUserOpt = userRepository.findByCpf("11111111111");

		assertThat(foundUserOpt).isPresent();
		assertThat(foundUserOpt.get().getId()).isEqualTo(user1.getId());
	}

	@Test
	@DisplayName("Deve retornar vazio ao buscar por CPF inexistente")
	void findByCpf_shouldReturnEmpty_whenCpfDoesNotExist() {
		Optional<User> foundUserOpt = userRepository.findByCpf("00000000000");

		assertThat(foundUserOpt).isNotPresent();
	}
}