package com.reservas.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LeasesResponse {
	private UUID id;
	private String name;
	private String type;
	private String description;
	private String hourValue;
	private Integer maxTime;
	private Integer minTime;
	private String createdAt;
}
