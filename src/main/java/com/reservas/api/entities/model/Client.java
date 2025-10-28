package com.reservas.api.entities.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"users", "reservations"})
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable=false)
	private String name;

	@Column
	private String email;

	@Column
	private String phone;

	@Column(nullable=false, unique=true)
	private String cpf;

	@Column(nullable=false, updatable=false)
	private LocalDateTime createdAt;

	@OneToOne(fetch=FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name="user_id", referencedColumnName="id", nullable=false, unique=true)
	private User user;

	@OneToMany(mappedBy="client", fetch=FetchType.LAZY)
	private List<Reservations> reservations;


	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
