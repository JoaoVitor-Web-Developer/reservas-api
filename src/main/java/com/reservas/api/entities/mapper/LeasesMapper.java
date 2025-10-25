package com.reservas.api.entities.mapper;

import com.reservas.api.entities.dto.LeasesRequest;
import com.reservas.api.entities.dto.LeasesResponse;
import com.reservas.api.entities.model.Leases;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
		leases.setCreatedAt(LocalDate.now());
		return leases;
	}

	public LeasesResponse toResponse(Leases entity) {
		LeasesResponse response = new LeasesResponse();
		response.setId(entity.getId());
		response.setName(entity.getName());
		response.setType(entity.getType());
		response.setDescription(entity.getDescription());
		response.setHourValue(entity.getHourValue());
		response.setMaxTime(entity.getMaxTime());
		response.setMinTime(entity.getMinTime());
		response.setCreatedAt(LocalDate.now());
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
