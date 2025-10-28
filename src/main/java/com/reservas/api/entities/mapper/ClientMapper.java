package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.ClientResponse;
import com.reservas.api.entities.dto.ClientUpdateRequest;
import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.model.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

	public ClientResponse toResponse(Client client) {
		if (client == null) return null;
		ClientResponse response = new ClientResponse();
		response.setId(client.getId());
		response.setName(client.getName());
		response.setEmail(client.getEmail());
		response.setPhone(client.getPhone());
		response.setCpf(client.getCpf());
		response.setCreatedAt(client.getCreatedAt());
		return response;
	}

	public Client fromRegisterRequest(UserRegisterRequest request) {
		Client client = new Client();
		client.setName(request.getName());
		client.setEmail(request.getEmail());
		client.setPhone(request.getPhone());
		client.setCpf(request.getCpf());
		return client;
	}

	public void updateEntityFromRequest(ClientUpdateRequest request, Client cliente) {
		if (request.getName() != null) {
			cliente.setName(request.getName());
		}
		if (request.getEmail() != null) {
			cliente.setEmail(request.getEmail());
		}
		if (request.getPhone() != null) {
			cliente.setPhone(request.getPhone());
		}
	}
}
