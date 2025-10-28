package com.reservas.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.service.LeasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeasesController.class)
class LeasesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private LeasesService leasesService;

	private UUID leaseId;
	private LeasesRequest leasesRequest;
	private LeasesResponse leasesResponse;
	private LocalDate testStartDate;
	private LocalDate testEndDate;
	private LocalDateTime testStartDateTime;
	private LocalDateTime testEndDateTime;

	@BeforeEach
	void setUp() {
		leaseId = UUID.randomUUID();
		testStartDate = LocalDate.of(2025, 11, 10);
		testEndDate = LocalDate.of(2025, 11, 12);
		testStartDateTime = testStartDate.atStartOfDay();
		testEndDateTime = testEndDate.atTime(23, 59, 59);

		leasesRequest = new LeasesRequest();
		leasesRequest.setName("Sala Teste");
		leasesRequest.setType(LeasesType.BEACH_HOUSE);
		leasesRequest.setDescription("Desc Teste");
		leasesRequest.setHourValue(BigDecimal.TEN);
		leasesRequest.setMaxTime(8);
		leasesRequest.setMinTime(1);

		leasesResponse = new LeasesResponse();
		leasesResponse.setId(leaseId);
		leasesResponse.setName("Sala Teste");
		leasesResponse.setType(LeasesType.BEACH_HOUSE);
		leasesResponse.setDescription("Desc Teste");
		leasesResponse.setHourValue(BigDecimal.TEN);
		leasesResponse.setMaxTime(8);
		leasesResponse.setMinTime(1);
	}

	@Test
	void getDisponibles_shouldReturnOkAndLeaseList() throws Exception {
		when(leasesService.listDisponibles(testStartDateTime, testEndDateTime))
				.thenReturn(List.of(leasesResponse));

		mockMvc.perform(get("/leases/disponibles")
				                .param("startDate", testStartDate.toString())
				                .param("endDate", testEndDate.toString()))
		       .andExpect(status().isOk())
		       .andExpect(content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(jsonPath("$", hasSize(1)))
		       .andExpect(jsonPath("$[0].id").value(leaseId.toString()));
	}

	@Test
	void getDisponibles_shouldReturnBadRequest_whenDatesInvalid() throws Exception {
		when(leasesService.listDisponibles(any(), any()))
				.thenThrow(new BusinessException("End date/time must be after start date/time"));

		mockMvc.perform(get("/leases/disponibles")
				                .param("startDate", testEndDate.toString())
				                .param("endDate", testStartDate.toString()))
		       .andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void createLease_shouldReturnOk_whenAuthenticatedUserCreatesLease() throws Exception {
		when(leasesService.save(any(LeasesRequest.class))).thenReturn(leasesResponse);

		mockMvc.perform(post("/leases")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.id").value(leaseId.toString()))
		       .andExpect(jsonPath("$.name").value("Sala Teste"));
	}

	@Test
	void createLease_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(post("/leases")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void updateLease_shouldReturnOk_whenAuthenticatedUserUpdatesLease() throws Exception {
		when(leasesService.update(eq(leaseId), any())).thenReturn(leasesResponse);

		mockMvc.perform(put("/leases/{id}", leaseId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.id").value(leaseId.toString()));
	}

	@Test
	void updateLease_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(put("/leases/{id}", leaseId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void deleteLease_shouldReturnNoContent_whenAuthenticatedUserDeletesLease() throws Exception {
		doNothing().when(leasesService).delete(leaseId);

		mockMvc.perform(delete("/leases/{id}", leaseId))
		       .andExpect(status().isNoContent());
	}

	@Test
	void deleteLease_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(delete("/leases/{id}", leaseId))
		       .andExpect(status().isUnauthorized());
	}
}
