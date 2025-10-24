package com.reservas.api.entities.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "reservations")
public class Leases {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private LeasesType type;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private BigDecimal hourValue;

	@Column(nullable = false)
	private Integer maxTime;

	@Column(nullable = false)
	private Integer minTime;

	@Column(nullable = false)
	private LocalDate createdAt;

	@OneToMany(mappedBy = "leases", fetch = FetchType.LAZY)
	private List<Reservations> reservations;
}
