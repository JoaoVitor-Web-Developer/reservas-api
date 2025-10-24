package com.reservas.api.entities.model;

public enum LeasesType {
	DAILY("Diária"),
	SEASONAL("Temporada"),
	LONG_TERM("Longo prazo"),
	SHORT_TERM("Curto prazo"),
	ROOM("Quarto"),
	ENTIRE_PROPERTY("Imóvel completo"),
	BED("Cama ou vaga"),
	EVENT_RENTAL("Locação para evento"),
	CORPORATE("Corporativo"),
	LUXURY("Luxo"),
	RURAL("Rural"),
	BEACH_HOUSE("Casa de praia"),
	MOUNTAIN_HOUSE("Casa de montanha"),
	MOTORHOME("Motorhome"),
	OTHER("Outro");

	private final String description;

	LeasesType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
