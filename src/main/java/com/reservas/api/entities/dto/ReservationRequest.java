package com.reservas.api.entities.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReservationRequest {
	private UUID leaseId;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
