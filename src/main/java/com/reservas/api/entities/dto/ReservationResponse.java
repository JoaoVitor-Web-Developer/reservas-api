package com.reservas.api.entities.dto;

import com.reservas.api.entities.enums.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReservationResponse {
	private UUID id;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private ReservationStatus status;
	private BigDecimal totalValue;
	private LocalDateTime createdAt;

	private ClientResponse client;
	private LeasesResponse leases;
	private UserResponse reservedBy;
}
