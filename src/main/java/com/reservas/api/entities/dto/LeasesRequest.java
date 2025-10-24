package com.reservas.api.entities.dto;

import com.reservas.api.entities.model.LeasesType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LeasesRequest {

	@NotBlank(message = "Name is required")
	private String name;

	private LeasesType type;

	private String description;

	@NotNull(message = "Hour value is required")
	@DecimalMin(value = "0.01", message = "Hour value must be greater than 0.01")
	private BigDecimal hourValue;

	private Integer maxTime;

	private Integer minTime;
}
