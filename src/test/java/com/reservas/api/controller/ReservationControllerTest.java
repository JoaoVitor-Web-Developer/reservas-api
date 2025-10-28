package com.reservas.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReservationController.class)
public class ReservationControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ReservationService reservationService;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void createReservation_ok() throws Exception {
		ReservationRequest req = new ReservationRequest();
		req.setLeaseId(UUID.randomUUID());

		ReservationResponse resp = new ReservationResponse();
		resp.setId(UUID.randomUUID());

		Mockito.when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(resp);

		mvc.perform(post("/reservations")
				            .contentType(MediaType.APPLICATION_JSON)
				            .content(mapper.writeValueAsString(req)))
		   .andExpect(status().isCreated())
		   .andExpect(jsonPath("$.id").exists());
	}

	@Test
	void cancelReservation_ok() throws Exception {
		UUID id = UUID.randomUUID();
		Mockito.doNothing().when(reservationService).cancelMyReservation(id);

		mvc.perform(delete("/reservations/" + id + "/cancel"))
		   .andExpect(status().isNoContent());
	}
}
