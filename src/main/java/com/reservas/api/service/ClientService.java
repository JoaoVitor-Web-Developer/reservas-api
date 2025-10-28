package com.reservas.api.service;

import com.reservas.api.entities.dto.ClientResponse;
import com.reservas.api.entities.dto.ClientUpdateRequest;
import com.reservas.api.entities.mapper.ClientMapper;
import com.reservas.api.entities.model.Client;
import com.reservas.api.entities.model.User;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.NotFoundException;
import com.reservas.api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

	private final ClientRepository clientRepository;
	private final ClientMapper clientMapper;

	@Transactional(readOnly = true)
	public ClientResponse getMyProfile() {
		User currentUser = getCurrentAuthenticatedUser();
		Client client = clientRepository.findByUser(currentUser)
				.orElseThrow(() -> new NotFoundException("User not found"));
		return clientMapper.toResponse(client);
	}

	@Transactional
	public ClientResponse updateMyProfile(ClientUpdateRequest request) {
		User currentUser = getCurrentAuthenticatedUser();
		Client existingClient = clientRepository.findByUser(currentUser)
				.orElseThrow(() -> new NotFoundException("User not found"));

		if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingClient.getEmail())) {
			clientRepository.findByEmail(request.getEmail()).ifPresent(clientWithEmail -> {
				if (!clientWithEmail.getEmail().equals(existingClient.getEmail())) {
					throw new BusinessException("There is already a client with this email: " + request.getEmail());
				}
			});
		}

		clientMapper.updateEntityFromRequest(request, existingClient);
		Client updatedClient = clientRepository.save(existingClient);
		return clientMapper.toResponse(updatedClient);
	}

	private User getCurrentAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
			throw new IllegalStateException("User not authenticated correctly");
		}
		return (User) authentication.getPrincipal();
	}
}
