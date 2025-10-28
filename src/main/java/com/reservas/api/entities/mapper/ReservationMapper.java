package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.ReservationRequest;
import com.reservas.api.entities.dto.ReservationResponse;
import com.reservas.api.entities.model.Reservations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMapper {

	private final ClientMapper clientMapper;
	private final LeasesMapper leasesMapper;
	private final UserMapper userMapper;

	public ReservationResponse toResponse(Reservations entity) {
		ReservationResponse response = new ReservationResponse();
		response.setId(entity.getId());
		response.setStartDate(entity.getStartDate());
		response.setEndDate(entity.getEndDate());
		response.setStatus(entity.getStatus());
		response.setTotalValue(entity.getTotalValue());
		response.setCreatedAt(entity.getCreatedAt());

		response.setClient(clientMapper.toResponse(entity.getClient()));
		response.setLeases(leasesMapper.toResponse(entity.getLeases()));
		response.setReservedBy(userMapper.toResponse(entity.getReservedBy()));
		return response;
	}

}
