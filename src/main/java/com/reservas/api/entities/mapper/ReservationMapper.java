package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.model.Reservations;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

	public Reservations toEntity(ReservationRequest request) {
		Reservations reservations = new Reservations();
		reservations.setUser(reservations.getUser());
		reservations.setLeases(reservations.getLeases());
		reservations.setStartDate(reservations.getStartDate());
		reservations.setEndDate(reservations.getEndDate());
		reservations.setStatus(reservations.getStatus());
		reservations.setTotalValue(reservations.getTotalValue());
		reservations.setCreatedAt(reservations.getCreatedAt());
		return reservations;
	}

	public ReservationResponse toResponse(Reservations entity) {
		ReservationResponse response = new ReservationResponse();
		response.setId(entity.getId());
		response.setUserId(entity.getUser().getId());
		response.setLeaseId(entity.getLeases().getId());
		response.setStartDate(entity.getStartDate());
		response.setEndDate(entity.getEndDate());
		response.setStatus(entity.getStatus());
		response.setTotalValue(entity.getTotalValue());
		response.setCreatedAt(entity.getCreatedAt());
		return response;
	}

	public void updateEntityFromRequest(ReservationRequest request, Reservations entity) {
		entity.setStartDate(request.getStartDate());
		entity.setEndDate(request.getEndDate());
		entity.setStatus(request.getStatus());
		entity.setTotalValue(request.getTotalValue());
	}
}
