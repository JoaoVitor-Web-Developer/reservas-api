package com.reservas.api.entities.dto;

import com.reservas.api.entities.enums.LeasesType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeasesResponse {
	private UUID id;
	private String name;
	private LeasesType type;
	private String description;
	private BigDecimal hourValue;
	private Integer maxTime;
	private Integer minTime;
	private LocalDate createdAt;
}
