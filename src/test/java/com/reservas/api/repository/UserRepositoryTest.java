package com.reservas.api.repository;

import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

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
		user1.setEmail("one@email.com");
		user1.setPassword("pass123");
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
	void findByEmail_shouldReturnUser_whenEmailExists() {
		Optional<User> foundUserOpt = userRepository.findByEmail("one@email.com");

		assertThat(foundUserOpt).isPresent();
		assertThat(foundUserOpt.get().getId()).isEqualTo(user1.getId());
		assertThat(foundUserOpt.get().getEmail()).isEqualTo("one@email.com");
	}

	@Test
	void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
		Optional<User> foundUserOpt = userRepository.findByEmail("nonexistent@email.com");

		assertThat(foundUserOpt).isNotPresent();
	}
}
