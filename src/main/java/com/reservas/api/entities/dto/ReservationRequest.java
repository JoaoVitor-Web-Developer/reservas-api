package com.reservas.api.entities.dto;

import com.reservas.api.entities.model.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReservationRequest {
	private UUID id;
	private UUID userId;
	private UUID leaseId;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private ReservationStatus status;
	private BigDecimal totalValue;
	private LocalDateTime createdDate;
}
