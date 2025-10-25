package com.reservas.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.enums.LeasesType;
import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.exception.BusinessException;
import com.reservas.api.exception.ResourceNotFoundException;
import com.reservas.api.service.LeasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeasesControllerTest.class)
class LeasesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private LeasesService leasesService;

	private UUID leaseId;
	private UUID userId;
	private LeasesRequest leasesRequest;
	private LeasesResponse leasesResponse;
	private ReservationResponse reservationResponse;
	private LocalDate testStartDate;
	private LocalDate testEndDate;
	private LocalDateTime testStartDateTime;
	private LocalDateTime testEndDateTime;


	@BeforeEach
	void setUp() {
		leaseId = UUID.randomUUID();
		userId = UUID.randomUUID();
		testStartDate = LocalDate.of(2025, 11, 10);
		testEndDate = LocalDate.of(2025, 11, 12);
		testStartDateTime = testStartDate.atStartOfDay();
		testEndDateTime = testEndDate.atTime(23,59,59);

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

		reservationResponse = new ReservationResponse();
		reservationResponse.setId(UUID.randomUUID());
		reservationResponse.setUserId(userId);
		reservationResponse.setLeaseId(leaseId);
		reservationResponse.setStartDate(testStartDateTime);
		reservationResponse.setEndDate(testEndDateTime);
		reservationResponse.setStatus(ReservationStatus.CONFIRMED);
		reservationResponse.setTotalValue(new BigDecimal("300.00"));
		reservationResponse.setCreatedAt(LocalDateTime.now());
	}

	// --- Testes para getDisponibles ---
	@Test
	@DisplayName("GET /leases/disponibles - Deve retornar 200 OK e lista de locações")
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
	@DisplayName("GET /leases/disponibles - Deve retornar 400 Bad Request para datas inválidas")
	void getDisponibles_shouldReturnBadRequest_whenDatesAreInvalidInService() throws Exception {
		when(leasesService.listDisponibles(any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenThrow(new BusinessException("End date/time must be after start date/time"));

		mockMvc.perform(get("/leases/disponibles")
				                .param("startDate", testEndDate.toString())
				                .param("endDate", testStartDate.toString()))
		       .andExpect(status().isBadRequest());
	}

	// --- Testes para createLease (Admin) ---
	@Test
	@DisplayName("POST /leases - Deve retornar 201 Created quando ADMIN cria locação")
	@WithMockUser(roles = "ADMIN")
	void createLease_shouldReturnCreated_whenAdminCreatesLease() throws Exception {
		when(leasesService.save(any(LeasesRequest.class))).thenReturn(leasesResponse);

		mockMvc.perform(post("/leases")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isCreated())
		       .andExpect(jsonPath("$.id").value(leaseId.toString()));
	}

	@Test
	@DisplayName("POST /leases - Deve retornar 403 Forbidden quando USER tenta criar locação")
	@WithMockUser(roles = "USER")
	void createLease_shouldReturnForbidden_whenUserTriesToCreateLease() throws Exception {

		mockMvc.perform(post("/leases")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("POST /leases - Deve retornar 401 Unauthorized quando não autenticado")
	void createLease_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(post("/leases")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isUnauthorized());
	}

	// --- Testes para updateLease (Admin) ---
	@Test
	@DisplayName("PUT /leases/{id} - Deve retornar 200 OK quando ADMIN atualiza locação")
	@WithMockUser(roles = "ADMIN")
	void updateLease_shouldReturnOk_whenAdminUpdatesLease() throws Exception {
		when(leasesService.update(eq(leaseId), any(LeasesRequest.class))).thenReturn(leasesResponse);

		mockMvc.perform(put("/leases/{id}", leaseId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isOk())
		       .andExpect(jsonPath("$.id").value(leaseId.toString()));
	}

	@Test
	@DisplayName("PUT /leases/{id} - Deve retornar 403 Forbidden quando USER tenta atualizar locação")
	@WithMockUser(roles = "USER")
	void updateLease_shouldReturnForbidden_whenUserTriesToUpdateLease() throws Exception {
		mockMvc.perform(put("/leases/{id}", leaseId)
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(leasesRequest)))
		       .andExpect(status().isForbidden());
	}

	// --- Testes para deleteLease (Admin) ---
	@Test
	@DisplayName("DELETE /leases/{id} - Deve retornar 204 No Content quando ADMIN deleta locação")
	@WithMockUser(roles = "ADMIN")
	void deleteLease_shouldReturnNoContent_whenAdminDeletesLease() throws Exception {
		doNothing().when(leasesService).delete(leaseId);

		mockMvc.perform(delete("/leases/{id}", leaseId))
		       .andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /leases/{id} - Deve retornar 403 Forbidden quando USER tenta deletar locação")
	@WithMockUser(roles = "USER")
	void deleteLease_shouldReturnForbidden_whenUserTriesToDeleteLease() throws Exception {
		mockMvc.perform(delete("/leases/{id}", leaseId))
		       .andExpect(status().isForbidden());
	}

	// --- Testes para hireLease (Autenticado) ---
	@Test
	@DisplayName("POST /leases/hire-lease/... - Deve retornar 201 Created quando usuário autenticado reserva")
	@WithMockUser // Simula usuário logado (USER ou ADMIN)
	void hireLease_shouldReturnCreated_whenAuthenticatedUserHires() throws Exception {
		when(leasesService.hireLease(eq(leaseId), eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenReturn(reservationResponse);

		mockMvc.perform(post("/leases/hire-lease/{id}/{userId}", leaseId, userId)
				                .param("startDate", testStartDate.toString())
				                .param("endDate", testEndDate.toString()))
		       .andExpect(status().isCreated())
		       .andExpect(jsonPath("$.id").value(reservationResponse.getId().toString()));
	}

	@Test
	@DisplayName("POST /leases/hire-lease/... - Deve retornar 401 Unauthorized quando não autenticado")
	void hireLease_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
		mockMvc.perform(post("/leases/hire-lease/{id}/{userId}", leaseId, userId)
				                .param("startDate", testStartDate.toString())
				                .param("endDate", testEndDate.toString()))
		       .andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("POST /leases/hire-lease/... - Deve retornar 400 Bad Request se serviço lançar BusinessException")
	@WithMockUser
	void hireLease_shouldReturnBadRequest_whenServiceThrowsBusinessException() throws Exception {
		when(leasesService.hireLease(eq(leaseId), eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenThrow(new BusinessException("Lease is not available"));

		mockMvc.perform(post("/leases/hire-lease/{id}/{userId}", leaseId, userId)
				                .param("startDate", testStartDate.toString())
				                .param("endDate", testEndDate.toString()))
		       .andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /leases/hire-lease/... - Deve retornar 404 Not Found se serviço lançar ResourceNotFoundException")
	@WithMockUser
	void hireLease_shouldReturnNotFound_whenServiceThrowsResourceNotFound() throws Exception {
		when(leasesService.hireLease(eq(leaseId), eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenThrow(new ResourceNotFoundException("Lease not found"));


		mockMvc.perform(post("/leases/hire-lease/{id}/{userId}", leaseId, userId)
				                .param("startDate", testStartDate.toString())
				                .param("endDate", testEndDate.toString()))
		       .andExpect(status().isNotFound());
	}

}