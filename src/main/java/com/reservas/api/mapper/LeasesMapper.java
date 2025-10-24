package com.reservas.api.mapper;

import com.reservas.api.dto.LeasesRequest;
import com.reservas.api.dto.LeasesResponse;
import com.reservas.api.model.Leases;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LeasesMapper {

	public Leases toEntity(LeasesRequest request) {
		Leases leases = new Leases();
		leases.setName(request.getName());
		leases.setType(request.getType());
		leases.setDescription(request.getDescription());
		leases.setHourValue(request.getHourValue());
		leases.setMaxTime(request.getMaxTime());
		leases.setMinTime(request.getMinTime());
		return leases;
	}

	public LeasesResponse toResponse(Leases entity) {
		LeasesResponse response = new LeasesResponse();
		response.setId(entity.getId());
		response.setName(entity.getName());
		response.setType(entity.getType().name());
		response.setDescription(entity.getDescription());
		response.setHourValue(entity.getHourValue());
		response.setMaxTime(entity.getMaxTime());
		response.setMinTime(entity.getMinTime());
		return response;
	}

	public void updateEntityFromRequest(LeasesRequest request, Leases entity) {
		entity.setName(request.getName());
		entity.setType(request.getType());
		entity.setDescription(request.getDescription());
		entity.setHourValue(request.getHourValue());
		entity.setMaxTime(request.getMaxTime());
		entity.setMinTime(request.getMinTime());
	}
}
