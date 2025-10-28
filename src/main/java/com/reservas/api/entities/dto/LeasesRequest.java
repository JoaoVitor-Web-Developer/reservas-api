package com.reservas.api.entities.dto;

import com.reservas.api.entities.enums.LeasesType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LeasesRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Type is required")
	private LeasesType type;

	private String description;

	@NotNull(message = "Hour value is required")
	@DecimalMin(value = "0.01", message = "Hour value must be greater than 0.01")
	private BigDecimal hourValue;

	@NotNull(message = "Max time is required")
	@Min(value = 1, message = "Max time must be greater than 0")
	private Integer maxTime;

	@NotNull(message = "Min time is required")
	@Min(value = 1, message = "Min time must be greater than 0")
	private Integer minTime;
}
