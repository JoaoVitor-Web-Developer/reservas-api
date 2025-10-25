package com.reservas.api.config;


import com.reservas.api.entities.enums.Role;
import com.reservas.api.entities.model.User;
import com.reservas.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${admin.default.email}")
	private String adminEmail;

	@Value("${admin.default.password}")
	private String adminPassword;

	@Override
	public void run(String... args) throws Exception {
		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			User admin = new User();
			admin.setName("ADMINISTRATOR");
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setCpf("00000000000");
			admin.setPhone("");
			admin.setRole(Role.ADMIN);
			admin.setCreatedAt(LocalDate.now());
			userRepository.save(admin);
		} else {
			log.info("Admin already exists");
		}
	}
}
