package com.reservas.api.service;

import com.reservas.api.entities.dto.ClientResponse;
import com.reservas.api.entities.dto.ClientUpdateRequest;
import com.reservas.api.entities.mapper.ClientMapper;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.NotFoundException;
import com.reservas.api.repository.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private ClientMapper clientMapper;

	@InjectMocks
	private ClientService clientService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void mockAuthenticatedUser(User user) {
		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(user);
		SecurityContext sc = mock(SecurityContext.class);
		when(sc.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(sc);
	}

	@Test
	void getMyProfile_whenClientExists_returnsResponse() {
		User user = new User();
		user.setId(java.util.UUID.randomUUID());
		user.setEmail("a@a.com");

		mockAuthenticatedUser(user);

		Client client = new Client();
		client.setId(java.util.UUID.randomUUID());
		client.setName("Joao");
		client.setUser(user);

		when(clientRepository.findByUser(user)).thenReturn(Optional.of(client));
		ClientResponse response = new ClientResponse();
		response.setId(client.getId());
		response.setName(client.getName());
		when(clientMapper.toResponse(client)).thenReturn(response);

		ClientResponse result = clientService.getMyProfile();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("Joao");
		verify(clientRepository).findByUser(user);
	}

	@Test
	void getMyProfile_whenClientNotFound_throwsNotFound() {
		User user = new User();
		user.setId(java.util.UUID.randomUUID());
		mockAuthenticatedUser(user);

		when(clientRepository.findByUser(user)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> clientService.getMyProfile())
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void updateMyProfile_happyPath_updatesAndReturns() {
		User user = new User();
		user.setId(java.util.UUID.randomUUID());
		mockAuthenticatedUser(user);

		Client existing = new Client();
		existing.setId(java.util.UUID.randomUUID());
		existing.setName("Old");
		existing.setEmail("old@a.com");

		when(clientRepository.findByUser(user)).thenReturn(Optional.of(existing));

		ClientUpdateRequest req = new ClientUpdateRequest();
		req.setName("New");
		req.setEmail("new@a.com");

		doAnswer(invocation -> {
			ClientUpdateRequest r = invocation.getArgument(0);
			Client c = invocation.getArgument(1);
			c.setName(r.getName());
			c.setEmail(r.getEmail());
			return null;
		}).when(clientMapper).updateEntityFromRequest(eq(req), eq(existing));

		Client saved = new Client();
		saved.setId(existing.getId());
		saved.setName("New");
		saved.setEmail("new@a.com");

		when(clientRepository.save(existing)).thenReturn(saved);
		ClientResponse resp = new ClientResponse();
		resp.setId(saved.getId());
		resp.setName(saved.getName());
		when(clientMapper.toResponse(saved)).thenReturn(resp);

		ClientResponse result = clientService.updateMyProfile(req);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("New");
		verify(clientRepository).save(existing);
	}
}
