package com.reservas.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.ClientResponse;
import com.reservas.api.entities.dto.ClientUpdateRequest;
import com.reservas.api.service.ClientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ClientService clientService;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void getMyProfile_ok() throws Exception {
		ClientResponse resp = new ClientResponse();
		resp.setId(UUID.randomUUID());
		resp.setName("Joao");

		Mockito.when(clientService.getMyProfile()).thenReturn(resp);

		mvc.perform(get("/client/me"))
		   .andExpect(status().isOk())
		   .andExpect(jsonPath("$.name").value("Joao"));
	}

	@Test
	void updateMyProfile_ok() throws Exception {
		ClientUpdateRequest req = new ClientUpdateRequest();
		req.setName("Updated");

		ClientResponse resp = new ClientResponse();
		resp.setId(UUID.randomUUID());
		resp.setName("Updated");

		Mockito.when(clientService.updateMyProfile(any(ClientUpdateRequest.class))).thenReturn(resp);

		mvc.perform(put("/client/me")
				            .contentType(MediaType.APPLICATION_JSON)
				            .content(mapper.writeValueAsString(req)))
		   .andExpect(status().isOk())
		   .andExpect(jsonPath("$.name").value("Updated"));
	}
}
